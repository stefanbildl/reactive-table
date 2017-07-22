import io.reactivex.*
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.operators.completable.CompletableError
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.DataOutputStream
import java.io.IOError
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.net.InetSocketAddress
import java.net.NetworkInterface


/**
 * Created by stefan on 10.07.17.
 */
internal class PresetTest {
    var api: SoundTouchApi? = null

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        api = SoundTouchApi(HttpUrl.parse("http://192.168.2.117:8090/")!!)
    }

    @org.junit.jupiter.api.AfterEach
    fun tearDown() {
        api = null
    }

    @Test
    fun testPresetRoute() {
        assertNotNull(api)
        System.out.print(api!!.getPresets().observeOn(Schedulers.trampoline()).test().assertNoErrors().values())
    }
    @Test
    fun testInfo() {
        assertNotNull(api)
        System.out.print(api!!.getInfo().test().assertNoErrors().values())
    }

    @Test
    fun datagramSocketTest() {
        val executor = Executors.newFixedThreadPool(256)
        var observable = Observable.empty<String>()
        for(i in 0 until 255) {
            observable = observable.concatWith(Single.fromFuture(executor.submit(Callable<String> {
                return@Callable (InetAddress.getLocalHost().hostAddress.replace("""\d+$""".toRegex(), i.toString())).apply {
                    Socket().connect(InetSocketAddress(this, 8090), 1000)
                }})).retry(5).toObservable().onErrorResumeNext(Observable.empty()))
        }

        executor.shutdown()
        val testobservable = observable.test()
        assertEquals(0, testobservable.errorCount())
        println("Values: ${testobservable.values()}")
    }

    @Test
    fun soundtouchDiscovererTest() {
        println(SoundTouchDiscoverer.create(InetAddress.getLocalHost()).discoverInformation().test().assertOf { it.valueCount() > 0 }.assertNoErrors().values())
    }

    @Test
    fun createMultiroomZoneTest() =
        SoundTouchDiscoverer.create(InetAddress.getLocalHost()).discoverInformation().blockingIterable()?.toList()?.apply {
            assert(this.size > 1) // we have at least 2 soundtouchs in the network
            val networkInfo = this.get(0).networkInfo[0]
            println("Creating Multiroom-Zone with master: <${this.get(0).name?:"unknown"}>...")
            networkInfo.url?.let {
                val api = SoundTouchApi(it)
                val list = this@apply
                api.postZone(networkInfo.macAddress!!, networkInfo.ipAddress!!, networkInfo.macAddress!!).andThen({
                    var o = Observable.empty<Info>();
                    for (i in 1 until list.size) list.get(i)?.let {
                        o = o.concatWith(api.addZoneSlave(networkInfo.macAddress!!, it.networkInfo.get(0).ipAddress!!, it.networkInfo.get(0).macAddress!!).toSingleDefault(it).toObservable())
                    }
                    o.test().assertValue { println("Adding slave <${it.name ?: return@assertValue false}>"); return@assertValue true }.assertNoErrors()
                }).test().assertNoErrors()
            }
        }


}