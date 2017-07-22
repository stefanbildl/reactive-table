import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import okhttp3.HttpUrl
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Created by stefan on 21.07.17.
 */

class SoundTouchDiscoverer private constructor(val inetAddress: InetAddress){
    companion object {
        fun create(inetAddress: InetAddress) = SoundTouchDiscoverer(inetAddress)
    }

    fun discover(): Observable<InetSocketAddress> {
        // open max of 255 threads (every ip in /24 subnet has one thread)
        val executor = Executors.newFixedThreadPool(255)
        var observable = Observable.empty<String>()
        // go through all ips in the subnet
        for(i in 0..254) {
            observable = observable.mergeWith(Single.fromFuture(executor.submit(Callable<String> {
                // replace last ip segment with current i
                return@Callable (InetAddress.getLocalHost().hostAddress.replace("""\d+$""".toRegex(), i.toString())).apply {
                    // tries to connect to a soundtouch on this ip address. If it fails, try once more.
                    // if inetaddress is not reachable, do not bother checking the port
                    if(!InetSocketAddress(this, 8090).address.isReachable(500))
                        throw Exception ("Not reachable")

                    Socket().connect(InetSocketAddress(this, 8090), 2000)
                    // if it fails two times, ignore this ip and complete
                    // if it succeeds. onNext the ip string
                }})).toObservable().retry(5).onErrorResumeNext(Observable.empty()))
        }
        // shutdown the executor gracefully
        executor.shutdown()
        // map each string to a InetSocketAddress
        return observable.map { t ->  InetSocketAddress(t, 8090)}
    }


    fun discoverInformation(): Observable<Info> = discover().flatMap<Info> {
        HttpUrl.parse("http://${it.address?.hostAddress}:8090/")?.let {
            return@flatMap SoundTouchApi(it).getInfo().toObservable()
        }
        return@flatMap Observable.empty()
    }
}