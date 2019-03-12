package com.woocommerce.android.ui.sitepicker

import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.analytics.AnalyticsTracker.Stat
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.util.WooLog.T
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.AccountActionBuilder
import org.wordpress.android.fluxc.generated.SiteActionBuilder
import org.wordpress.android.fluxc.generated.WCCoreActionBuilder
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.AccountStore
import org.wordpress.android.fluxc.store.AccountStore.OnAccountChanged
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.OnSiteChanged
import org.wordpress.android.fluxc.store.WooCommerceStore
import org.wordpress.android.fluxc.store.WooCommerceStore.OnApiVersionFetched
import org.wordpress.android.fluxc.store.WooCommerceStore.OnWCSimpleSitesFetched
import javax.inject.Inject

class SitePickerPresenter @Inject constructor(
    private val dispatcher: Dispatcher,
    private val accountStore: AccountStore,
    private val siteStore: SiteStore,
    private val wooCommerceStore: WooCommerceStore
) : SitePickerContract.Presenter {
    private var view: SitePickerContract.View? = null
    private var fetchingSiteId: Long = 0

    override fun takeView(view: SitePickerContract.View) {
        dispatcher.register(this)
        this.view = view
    }

    override fun dropView() {
        dispatcher.unregister(this)
        view = null
    }

    /**
     * This fetches a list of "simple sites" that are Woo stores - these are lightweight models that
     * contain only a subset of the SiteModel properties (id, url, and name)
     */
    override fun fetchWooSimpleSites() {
        dispatcher.dispatch(WCCoreActionBuilder.newFetchWooSimpleSitesAction())
    }

    override fun getWooSites(): List<SiteModel> = wooCommerceStore.getWooCommerceSites()

    override fun getUserAvatarUrl() = accountStore.account?.avatarUrl

    override fun getUserName() = accountStore.account?.userName

    override fun getUserDisplayName() = accountStore.account?.displayName

    override fun logout() {
        dispatcher.dispatch(AccountActionBuilder.newSignOutAction())
        dispatcher.dispatch(SiteActionBuilder.newRemoveWpcomAndJetpackSitesAction())
    }

    override fun userIsLoggedIn(): Boolean {
        return accountStore.hasAccessToken()
    }

    override fun fetchWooSite(site: SiteModel) {
        fetchingSiteId = site.siteId
        dispatcher.dispatch(SiteActionBuilder.newFetchSiteAction(site))
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAccountChanged(event: OnAccountChanged) {
        if (!event.isError && !userIsLoggedIn()) {
            view?.didLogout()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWCSimpleSitesFetched(event: OnWCSimpleSitesFetched) {
        if (!event.isError) {
            AnalyticsTracker.track(
                    Stat.SITE_PICKER_STORES_SHOWN,
                    mapOf(AnalyticsTracker.KEY_NUMBER_OF_STORES to event.simpleSites.size)
            )

            // convert the simple sites to a list of site models
            val sites = ArrayList<SiteModel>()
            event.simpleSites.forEach {
                sites.add(it.toSiteModel())
            }

            view?.showStoreList(sites)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSiteChanged(event: OnSiteChanged) {
        if (event.isError) {
            WooLog.e(T.LOGIN, "Error fetching site $fetchingSiteId " +
                    "${event.error?.type} - ${event.error?.message}")
            view?.siteFetchError()
        } else {
            siteStore.getSiteBySiteId(fetchingSiteId)?.let { site ->
                // site has been fetched, so fetch its settings and verify its running the right version
                dispatcher.dispatch(WCCoreActionBuilder.newFetchSiteSettingsAction(site))
                dispatcher.dispatch(WCCoreActionBuilder.newFetchSiteApiVersionAction(site))
            } ?: view?.siteFetchError()
        }

        fetchingSiteId = 0L
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApiVersionFetched(event: OnApiVersionFetched) {
        if (event.isError) {
            WooLog.e(T.LOGIN, "Error fetching apiVersion for site [${event.site.siteId} : ${event.site.name}]! " +
                    "${event.error?.type} - ${event.error?.message}")
            view?.siteVerificationError(event.site)
            return
        }

        // Check for empty API version as well (which may not result in an error from the api)
        if (event.apiVersion.isBlank()) {
            WooLog.e(T.LOGIN, "Empty apiVersion for site [${event.site.siteId} : ${event.site.name}]!")
            view?.siteVerificationError(event.site)
            return
        }

        if (event.apiVersion == WooCommerceStore.WOO_API_NAMESPACE_V3) {
            view?.siteVerificationPassed(event.site)
        } else {
            view?.siteVerificationFailed(event.site)
        }
    }
}
