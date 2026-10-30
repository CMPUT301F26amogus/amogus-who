interface ViewObserver<M> {
    fun update( model: M )
}

abstract open class View: ViewObserver<Model>{
    override fun update(model: Model) {

    }
    //Generic Component Composables go here
}