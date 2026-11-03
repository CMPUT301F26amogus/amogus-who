
interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    override fun addView(v: View){

    }
    override fun removeView(v: View){

    }
}