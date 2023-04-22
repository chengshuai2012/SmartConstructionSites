package com.aoecloud.smartconstructionsites.fragment

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.aoecloud.smartconstructionsites.R
import com.aoecloud.smartconstructionsites.bean.BannerBean
import com.aoecloud.smartconstructionsites.bean.ProjectDataX
import com.aoecloud.smartconstructionsites.utils.GlideUtils
import com.bumptech.glide.load.resource.bitmap.VideoDecoder
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerAdapter

class MineBannerAdapter constructor(
    val context: Context,
    val lifecycle: Lifecycle,
    val list: List<ProjectDataX> = emptyList()
) : BannerAdapter<ProjectDataX, MineBannerAdapter.MineBannerViewHolder>(list) {
    private val mInflater = LayoutInflater.from(context)


    var mItemClickCallBack: OnItemClickListener? = null

    var recyclerViewOrNull: RecyclerView? = null
        private set

    private val options = RequestOptions()
        .set(VideoDecoder.FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST)
        .frame(1 * 1000 * 1000)
        .fitCenter()


    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): MineBannerViewHolder {
        val itemView =
            mInflater.inflate(R.layout.adapter_mine_banner, parent, false)
        return MineBannerViewHolder(itemView)
    }


    override fun onBindView(
        holder: MineBannerViewHolder,
        data: ProjectDataX?,
        position: Int,
        size: Int
    ) {
        GlideUtils.loadNormal(data?.image?:"",holder.pic)
        holder.title.text = data?.project_name
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerViewOrNull = recyclerView
    }

    /**
     * 获取指定位置的指定Id的view
     */
    fun getViewByPosition(position: Int, @IdRes viewId: Int): View? {
        val recyclerView = recyclerViewOrNull ?: return null
        val viewHolder = recyclerView.findViewHolderForLayoutPosition(position) as? MineBannerViewHolder
            ?: return null
        return viewHolder.itemView.findViewById(viewId)
    }


    /**
     * 个人中中视频的BannerViewHolder
     */
    class MineBannerViewHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val pic: ImageView = itemView.findViewById(R.id.pic)
        val title: TextView = itemView.findViewById(R.id.title)
    }

    interface OnItemClickListener {
        fun clickItem(pos: Int, item: ProjectDataX?, srcView: ImageView)
    }
}

