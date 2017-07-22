import io.reactivex.Single
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.http.GET

/**
 * Created by stefan on 10.07.17.
 */

@Root(strict = false)
data class NetworkInfo(
        @get:Attribute
        @set:Attribute
        var type: String? = null,

        @get:Element
        @set:Element
        var macAddress: String? = null,

        @get:Element
        @set:Element
        var ipAddress: String? = null)
{
    val url get() = okhttp3.HttpUrl.parse("http://${ipAddress}:8090")
    val api get() = url?.let{return@let SoundTouchApi(it)}
}

@Root(strict = false)
data class Component(
        @get:Element
        @set:Element
        var componentCategory: String? = null,

        @get:Element(required = false)
        @set:Element(required = false)
        var softwareVersion: String? = null,

        @get:Element
        @set:Element
        var serialNumber: String? = null
)

@Root(strict = false)
data class Info(
        @get:Attribute
        @set:Attribute
        var deviceID: String? = null,

        @get:Element
        @set:Element
        var name: String? = null,

        @get:Element
        @set:Element
        var type: String? = null,

        @get:ElementList
        @set:ElementList
        var components : ArrayList<Component> = arrayListOf(),

        @get:ElementList(inline = true)
        @set:ElementList(inline = true)
        var networkInfo : ArrayList<NetworkInfo> = arrayListOf())
{
        val url get() = if (networkInfo.size > 0) networkInfo[0].url else null
        val api get() = if (networkInfo.size > 0) networkInfo[0].api else null
}
interface InfoInterface {
        @GET("info")
        fun getInfo(): Single<Info>
}