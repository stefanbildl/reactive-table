import io.reactivex.Completable
import io.reactivex.Single
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by stefan on 10.07.17.
 */



@Root(name="ContentItem", strict = false)
data class ContentItem(
    @get:Attribute
    @set:Attribute
    var unusedField: Int? = null,

    @get:Attribute
    @set:Attribute
    var source: String? = null,

    @get:Attribute
    @set:Attribute
    var location: String? = null,

    @get:Attribute
    @set:Attribute
    var sourceAccount: String? = null,

    @get:Attribute(name="isPresetable")
    @set:Attribute(name="isPresetable")
    var isPresetable: String? = null,

    @get:Element
    @set:Element
    var itemName: String? = null
)

@Root(name="preset", strict = false)
data class Preset(
    @get:Attribute(name="id")
    @set:Attribute(name="id")
    var id: Int? = null,

    @get:Attribute(name="createdOn")
    @set:Attribute(name="createdOn")
    var createdOn: Int? = null,

    @get:Attribute(name="updatedOn")
    @set:Attribute(name="updatedOn")
    var updatedOn: Int? = null,

    @get:ElementList(inline=true, required = false)
    @set:ElementList(inline=true, required = false)
    var contentItems: ArrayList<ContentItem> = arrayListOf()
)

@Root(name="presets", strict = false)
data class Presets(
    @get:ElementList(required=true, inline=true)
    @set:ElementList(required=true, inline=true)
    var list:ArrayList<Preset> = arrayListOf())

interface PresetInterface {
    @GET("presets")
    fun getPresets(): Single<Presets>
}

interface SelectInterface {
    @POST("select")
    fun select(@Body contentItem: ContentItem): Completable
}
