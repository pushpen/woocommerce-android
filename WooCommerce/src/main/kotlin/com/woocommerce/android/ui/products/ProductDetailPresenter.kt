package com.woocommerce.android.ui.products

import com.woocommerce.android.network.ConnectionChangeReceiver
import com.woocommerce.android.tools.NetworkStatus
import com.woocommerce.android.tools.SelectedSite
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.WCProductAction.FETCH_SINGLE_PRODUCT
import org.wordpress.android.fluxc.generated.WCProductActionBuilder
import org.wordpress.android.fluxc.model.WCProductModel
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.OnProductChanged
import javax.inject.Inject

class ProductDetailPresenter @Inject constructor(
    private val dispatcher: Dispatcher,
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite,
    private val networkStatus: NetworkStatus
) : ProductDetailContract.Presenter {
    companion object {
        private val TAG: String = ProductDetailPresenter::class.java.simpleName
    }

    override var remoteProductId = 0L
    private var view: ProductDetailContract.View? = null

    override fun takeView(view: ProductDetailContract.View) {
        this.view = view
        dispatcher.register(this)
        ConnectionChangeReceiver.getEventBus().register(this)
    }

    override fun dropView() {
        view = null
        dispatcher.unregister(this)
        ConnectionChangeReceiver.getEventBus().unregister(this)
    }

    override fun getProduct(remoteProductId: Long): WCProductModel? =
            productStore.getProductByRemoteId(selectedSite.get(), remoteProductId)

    override fun fetchProduct(remoteProductId: Long) {
        this.remoteProductId = remoteProductId
        val payload = WCProductStore.FetchSingleProductPayload(selectedSite.get(), remoteProductId)
        dispatcher.dispatch(WCProductActionBuilder.newFetchSingleProductAction(payload))
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductChanged(event: OnProductChanged) {
        // product was just fetched, show its image
        if (event.causeOfChange == FETCH_SINGLE_PRODUCT) {
            if (event.isError) {
                view?.showFetchProductError()
            } else {
                getProduct(remoteProductId)?.let {
                    view?.showProduct(it)
                } ?: view?.showFetchProductError()
            }
        }
    }
}