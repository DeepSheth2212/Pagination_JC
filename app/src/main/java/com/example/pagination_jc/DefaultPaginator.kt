package com.example.pagination_jc

//here we are making general class for pagination
//not only for pagination of specific data that we get from our repository class
//we may get data from diff sources like apis etc...

//that's why we are using generics
//where Key shows the key of page(here page)
//and Item shows the item of page(here ListItem)
class DefaultPaginator<Key,Item>(
    private val initialKey : Key,
    private inline val onLoadUpdated : (Boolean)-> Unit, //this call back is called when change the boolean
    // when we load the items or when we completed the loading of items //typically used to show little progress bar
    private inline val onRequest : suspend (nextKey : Key) -> Result<List<Item>>, //defines how we get the next load of items given the key
    private inline val getNextKey : (List<Item>) -> Key,
    //this List<Item> may contain the info regarding to next page ..
    //if our key is in int form in that case it will be easier bcz the next page will be current page + 1
    //but some apis also return different page id's (String type) for diff pages .... in this case api may return the information regarding the next page id in the list it sends ...

    private inline val onError : suspend(Throwable?)->Unit,
    private inline val onSuccess : suspend (items:List<Item> , newKey : Key) -> Unit

) : Paginator<Key,Item>{

    private var currentKey = initialKey
    private var isMakingRequest = false
    override suspend fun loadNextItem() {
        if(isMakingRequest){
            return
        }
        isMakingRequest = true //bcz we are making request right now
        onLoadUpdated(true) //we updated loading status
        var result = onRequest(currentKey)
        isMakingRequest = false
        val items = result.getOrElse {
            onError(it)
            onLoadUpdated(false)
            return
        }//if everything works fine we get the list of items in items var but otherwise(if error occurred) the else block will execute

        currentKey = getNextKey(items)
        onSuccess(items , currentKey)
        onLoadUpdated(false)
    }

    override fun reset() {
        currentKey = initialKey
    }
}