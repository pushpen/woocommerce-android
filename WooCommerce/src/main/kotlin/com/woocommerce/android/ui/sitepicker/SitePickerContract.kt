package com.woocommerce.android.ui.sitepicker

import com.woocommerce.android.ui.base.BasePresenter
import com.woocommerce.android.ui.base.BaseView
import org.wordpress.android.fluxc.model.SiteModel

interface SitePickerContract {
    interface Presenter : BasePresenter<View> {
        fun fetchWooSimpleSites()
        fun fetchWooSite(site: SiteModel)
        fun getWooSites(): List<SiteModel>
        fun getUserAvatarUrl(): String?
        fun getUserName(): String?
        fun getUserDisplayName(): String?
        fun logout()
        fun userIsLoggedIn(): Boolean
    }

    interface View : BaseView<Presenter> {
        fun showUserInfo()
        fun showStoreList(sites: List<SiteModel>)
        fun didLogout()
        fun siteSelected(site: SiteModel)
        fun siteFetchError()
        fun siteVerificationPassed(site: SiteModel)
        fun siteVerificationFailed(site: SiteModel)
        fun siteVerificationError(site: SiteModel)
    }
}
