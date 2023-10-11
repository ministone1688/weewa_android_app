package com.xh.hotme.widget

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xh.hotme.R


/**
 *    date   : 2023/6/19
 *    desc   :吸顶
 */
class TitleItemDecoration(
    private val context: Context,
    private val callback: TitleDecorationCallback
) : RecyclerView.ItemDecoration() {

    private val mTitleHeight: Int

    // 直接引用标题ViewHolder对应的layout -- item_head.xml
    private val titleLayout: View = LayoutInflater.from(context).inflate(R.layout.video_list_item_head, null)
    private val tvTitle = titleLayout.findViewById<TextView>(R.id.tvTopTitle)

    init {
        /**
         * 手动测量头部所需高度，这里提醒一下，item_head.xml 这个layout文件里的控件的宽高，不要用 wrap_content 和 match_parent
         * 要明确标出宽高，才能测量出来
         * */
        titleLayout.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED)
        mTitleHeight = titleLayout.measuredHeight
    }

    /**
     *      重写 onDrawOver()
     * */
    override fun onDrawOver(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(canvas, recyclerView, state)
        // 获取第一个可见 Item 对应的 Position
        val firstVisiblePosition = findFirstVisibleItemPosition(recyclerView.layoutManager!!)
        if (firstVisiblePosition <= -1 || firstVisiblePosition >= recyclerView.adapter!!.itemCount - 1) {
            // 安全检测，防止越界
            return
        }
        // 获取第一个可见 Item 对应 View
        val firstVisibleView =
            recyclerView.findViewHolderForAdapterPosition(firstVisiblePosition)!!.itemView

        // 因为我们要绘制在列表顶部，所以先获取RecycleView 左右上 三个坐标
        val left = recyclerView.paddingLeft
        val right = recyclerView.width - recyclerView.paddingRight
        var top = recyclerView.paddingTop

        /**
         * 这里要判断，我们下一行是否是标题，如果是，原来绘制在屏幕上的标题，就得推出屏幕顶部
         * 至于推出屏幕顶部距离多少，就得看下一个标题已经推进吸顶区域大多
         * 下面就是获取下一个标题推进吸顶区域的高度是多大
         * */
        if (nextLineIsTitle(
                firstVisibleView,
                firstVisiblePosition,
                recyclerView
            ) && firstVisibleView.bottom < mTitleHeight
        ) {
            top = if (mTitleHeight <= firstVisibleView.height) {
                val d = firstVisibleView.height - mTitleHeight
                /**
                 * 通常来说，这里这个d 是等于0的，因为吸顶区域的高度一般都会和列表里面的标题的高度是一模一样的
                 * firstVisibleView.top 就是第一个可见Item 的顶部，这里的top如果是负数，即说明 firstVisibleView已经有一部分
                 * 滑出屏幕了，这时候吸顶绘制的区域，也要跟随它
                 * */
                firstVisibleView.top + d
            } else {
                val d = mTitleHeight - firstVisibleView.height
                firstVisibleView.top - d
            }
        }
        // 去绘制头部
        drawTitle(canvas, top, firstVisiblePosition, left, right)
    }

    private fun drawTitle(canvas: Canvas, top: Int, position: Int, left: Int, right: Int) {
        // 设置偏移，dx=0,即代表向左对齐
        canvas.translate(0f, top.toFloat())
        tvTitle.text = callback.getHeadTitle(position)
        titleLayout.layout(left, 0, right, mTitleHeight)
        titleLayout.draw(canvas)
    }


    private fun findFirstVisibleItemPosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            is LinearLayoutManager -> {
                layoutManager.findFirstVisibleItemPosition()
            }
            is GridLayoutManager -> {
                layoutManager.findFirstVisibleItemPosition()
            }
            is StaggeredGridLayoutManager -> {
                layoutManager.findFirstVisibleItemPositions(null)[0]
            }
            else -> {
                throw RuntimeException("咱不支持 类型为：${layoutManager.javaClass.name} 的LayoutManager ,可以自己判断类型，转成自己的LayoutManager，去获取第一个可见Item的position ")
            }
        }
    }

    /**
     *      网格布局应该算下一行是否是Title,而不是算下一个Position
     *      @param 当前Item
     *      @param 当前position
     *      @param parent
     * */
    private fun nextLineIsTitle(
        currentView: View,
        currentPosition: Int,
        parent: RecyclerView
    ): Boolean {
        for (nextLinePosition in currentPosition + 1 until parent.adapter!!.itemCount) {
            val nextItemView = parent.findViewHolderForAdapterPosition(nextLinePosition)!!.itemView
            if (nextItemView.bottom > currentView.bottom) {
                // 找到下一行的 Position
                return callback.isHeadItem(currentPosition,nextLinePosition)
            }
        }
        return false
    }

    interface TitleDecorationCallback {
        /**
         *      当前 position 对应的ViewHolder 是否是标题类型
         * */
        fun isHeadItem(curposition: Int,position: Int): Boolean

        /**
         *      当前 position 对应的ViewHolder 是属于哪一种标题类型
         * */
        fun getHeadTitle(position: Int): String
    }
}