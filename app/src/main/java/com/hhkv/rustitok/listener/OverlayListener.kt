
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.hhkv.rustitok.MainActivity

class OverlayDragListener(
    private val context: Context,
    private val windowManager: WindowManager,
    private val params: WindowManager.LayoutParams
) : View.OnTouchListener {

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    private var isMove = true

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMove = false
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(view, params)
                isMove = true
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isMove){
                    val clickIntent = Intent(
                        context,
                        MainActivity::class.java
                    )
                    context.startActivity(clickIntent)
                }
                isMove = false
                return true
            }
        }
        return false
    }
}
