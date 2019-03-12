package com.woocommerce.android.ui.sitepicker

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.woocommerce.android.R
import com.woocommerce.android.ui.sitepicker.SitePickerAdapter.SiteViewHolder
import com.woocommerce.android.util.StringUtils
import kotlinx.android.synthetic.main.site_picker_item.view.*
import org.wordpress.android.fluxc.model.WCSimpleSiteModel

class SitePickerAdapter(private val context: Context, private val listener: OnSiteClickListener) :
        RecyclerView.Adapter<SiteViewHolder>() {
    var simpleSiteList: List<WCSimpleSiteModel> = ArrayList()
        set(value) {
            if (!isSameSiteList(value)) {
                field = value
                notifyDataSetChanged()
            }
        }
    var selectedSiteId: Long = 0
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    interface OnSiteClickListener {
        fun onSiteClick()
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return simpleSiteList[position].siteId
    }

    override fun getItemCount(): Int {
        return simpleSiteList.size
    }

    fun getSelectedSite(): WCSimpleSiteModel? {
        simpleSiteList.forEach {
            if (it.siteId == selectedSiteId) return it
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        return SiteViewHolder(LayoutInflater.from(context).inflate(R.layout.site_picker_item, parent, false))
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val simpleSite = simpleSiteList[position]
        holder.radio.visibility = if (simpleSiteList.size > 1) View.VISIBLE else View.GONE
        holder.radio.isChecked = simpleSite.siteId == selectedSiteId
        holder.txtSiteName.text = if (!TextUtils.isEmpty(simpleSite.name)) simpleSite.name else context.getString(R.string.untitled)
        holder.txtSiteDomain.text = StringUtils.getSiteDomainAndPath(simpleSite.url)
        if (itemCount > 1) {
            holder.itemView.setOnClickListener {
                if (selectedSiteId != simpleSite.siteId) {
                    listener.onSiteClick()
                    selectedSiteId = simpleSite.siteId
                }
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    /**
     * returns true if the passed list of sites is the same as the current list
     */
    private fun isSameSiteList(simpleSites: List<WCSimpleSiteModel>): Boolean {
        if (simpleSites.size != simpleSiteList.size) {
            return false
        }

        simpleSites.forEach {
            if (!containsSite(it)) {
                return false
            }
        }

        return true
    }

    /**
     * Returns true if the passed order is in the current list of orders
     */
    private fun containsSite(site: WCSimpleSiteModel): Boolean {
        simpleSiteList.forEach {
            if (it.siteId == site.siteId) {
                return true
            }
        }
        return false
    }

    class SiteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radio: RadioButton = view.radio
        val txtSiteName: TextView = view.text_site_name
        val txtSiteDomain: TextView = view.text_site_domain

        init {
            radio.isClickable = false
        }
    }
}
