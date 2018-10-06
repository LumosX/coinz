package eu.zerovector.coinz.Components

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import eu.zerovector.coinz.Data.bool
import eu.zerovector.coinz.R




@SuppressLint("SetTextI18n")
class ExpandableMessageView : LinearLayout {

    private var currentView: View
    private val btnExpand: Button
    private val lblMessage: TextView
    private var isAnimating: bool = false
    private var messageTargetHeight: Int


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        // Inflate layout
        currentView = inflate(context, R.layout.layout_expandable_message, this)

        btnExpand = currentView.findViewById(R.id.btnExpand)
        lblMessage = currentView.findViewById(R.id.lblMessage)
        messageTargetHeight = lblMessage.height

        // Hook listener up and hide the message
        btnExpand.setOnClickListener(::onButtonClicked)
        lblMessage.visibility = View.GONE
    }


    @SuppressLint("SetTextI18n")
    private fun onButtonClicked(view: View) {
        if (isAnimating) return

        val isCurrentlyExpanded = lblMessage.isShown
        isAnimating = true

        // If the view is currently invisible, show it so that the animation can be seen
        if (!isCurrentlyExpanded) lblMessage.visibility = View.VISIBLE

        val targetHeight = if (isCurrentlyExpanded) 0 else messageTargetHeight
        val startingHeight = if (isCurrentlyExpanded) messageTargetHeight else 0

        val anim = ValueAnimator.ofInt(startingHeight, targetHeight)
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams = lblMessage.layoutParams
            layoutParams.height = value
            lblMessage.layoutParams = layoutParams
        }
        anim.addListener(object: Animator.AnimatorListener {
            var targetVisibility = false

            override fun onAnimationRepeat(animation: Animator?) { }

            override fun onAnimationCancel(animation: Animator?) { }

            override fun onAnimationStart(animation: Animator?) {
                targetVisibility = !isCurrentlyExpanded

            }

            override fun onAnimationEnd(animation: Animator?) {
                // If the view is not supposed to be visible, make it invisible now, after it's "contracted"
                if (!targetVisibility) lblMessage.visibility = View.GONE
                isAnimating = false
            }
        })
        anim.duration = 200
        anim.start()

    }

    fun SetSenderAndSubject(sender: String, subject: String) {
        btnExpand.text = "From: $sender\nSubject: $subject"
    }

    fun SetMessageText(message: String) {
        lblMessage.text = message
    }


}

