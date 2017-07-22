import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

/**
 * Created by stefan on 10.07.17.
 */

class SoundTouchApi(baseUrl: HttpUrl) {
    private val retrofit: Retrofit

    init {
        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    fun getPresets(): Observable<Preset> {
        val restClient = retrofit.create(PresetInterface::class.java)
        return restClient.getPresets().flatMapObservable { Observable.fromIterable(it.list) }
    }

    fun select(contentItem: ContentItem): Completable {
        val restClient = retrofit.create(SelectInterface::class.java)
        return restClient.select(contentItem)
    }

    fun select(source: String, sourceAccount: String, location: String, itemName: String) {
        select(ContentItem(source = source, sourceAccount = sourceAccount, location = location, itemName = itemName))
    }

    private fun postZone(zone : Zone): Completable {
        val restClient = retrofit.create(ZoneInterface::class.java)
        return restClient.postZone(zone)
    }

    private fun addZoneSlave(zone : Zone) : Completable {
        val restClient = retrofit.create(ZoneInterface::class.java)
        return restClient.addZoneSlave(zone)
    }

    fun postZone(masterMacAddress: String, senderIPAddress: String, macAddress: String): Completable{
        return postZone(Zone(master=masterMacAddress, senderIPAddress = senderIPAddress, list = arrayListOf(Member(senderIPAddress, macAddress))))
    }

    fun addZoneSlave(master: String, memberIPAddress: String, memberMacAddress: String): Completable {
        return addZoneSlave(Zone(master = master, list = arrayListOf(Member(memberIPAddress, memberMacAddress))))
    }

    fun getInfo(): Single<Info> {
        val restClient = retrofit.create(InfoInterface::class.java)
        return restClient.getInfo()
    }
}