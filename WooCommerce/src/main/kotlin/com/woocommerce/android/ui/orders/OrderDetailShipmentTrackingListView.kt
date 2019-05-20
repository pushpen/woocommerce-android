package com.woocommerce.android.ui.orders

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.woocommerce.android.R
import com.woocommerce.android.ui.base.UIMessageResolver
import kotlinx.android.synthetic.main.order_detail_shipment_tracking_list.view.*
import org.wordpress.android.fluxc.model.WCOrderShipmentTrackingModel

class OrderDetailShipmentTrackingListView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.order_detail_shipment_tracking_list, this)
    }

    fun initView(trackings: List<WCOrderShipmentTrackingModel>, uiMessageResolver: UIMessageResolver) {
        val viewManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val viewAdapter = ShipmentTrackingListAdapter(trackings, uiMessageResolver)

        shipmentTrack_items.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            addItemDecoration(
                    androidx.recyclerview.widget.DividerItemDecoration(
                            context,
                            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                    )
            )
            adapter = viewAdapter
        }
    }

    class ShipmentTrackingListAdapter(
        private val trackings: List<WCOrderShipmentTrackingModel>,
        private val uiMessageResolver: UIMessageResolver
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ShipmentTrackingListAdapter.ViewHolder>() {
        class ViewHolder(val view: OrderDetailShipmentTrackingItemView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: OrderDetailShipmentTrackingItemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.order_detail_shipment_tracking_list_item, parent, false)
                    as OrderDetailShipmentTrackingItemView
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.view.initView(trackings[position], uiMessageResolver)
        }

        override fun getItemCount() = trackings.size
    }
}
