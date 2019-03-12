package com.woocommerce.android.ui.sitepicker

import com.woocommerce.android.ui.base.BasePresenter
import com.woocommerce.android.ui.base.BaseView
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.WCSimpleSiteModel

interface SitePickerContract {
    interface Presenter : BasePresenter<View> {
        fun fetchWooSites()
        fun fetchWooSite(site: SiteModel)
        fun fetchWooSiteSettings(site: SiteModel)
        fun getWooSites(): List<SiteModel>
        fun getUserAvatarUrl(): String?
        fun getUserName(): String?
        fun getUserDisplayName(): String?
        fun logout()
        fun userIsLoggedIn(): Boolean
    }

    interface View : BaseView<Presenter> {
        fun showUserInfo()
        fun showStoreList(simpleSites: List<WCSimpleSiteModel>)
        fun didLogout()
        fun siteSelected()
        fun siteFetchError()
        fun siteVerificationPassed(site: SiteModel)
        fun siteVerificationFailed(site: SiteModel)
        fun siteVerificationError(site: SiteModel)
    }
}
