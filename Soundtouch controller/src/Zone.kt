import io.reactivex.Completable
import org.simpleframework.xml.*
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by stefan on 10.07.17.
 */


/*
<zone master="$MACADDR" senderIPAddress="$IPADDR">
<member ipaddress="$IPADDR">$MACADDR</member>
...
</zone>
*/

@Element
data class Member (
    @get:Attribute
    @set:Attribute
    var ipaddress: String,

    @get:Text
    @set:Text
    var macAddress: String
)

@Root
data class Zone(
    @get:Attribute
    @set:Attribute
    var master: String? = null,

    @get:Attribute(required = false)
    @set:Attribute(required = false)
    var senderIPAddress: String? = null,

    @get:ElementList(inline = true)
    @set:ElementList(inline = true)
    var list:ArrayList<Member> = arrayListOf())

interface ZoneInterface {
    @POST("setZone")
    fun postZone(@Body zone: Zone): Completable;

    @POST("addZoneSlave")
    fun addZoneSlave(@Body zone: Zone): Completable;
}
