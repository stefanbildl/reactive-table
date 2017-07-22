import com.stefanbildl.FilterBasedFlowableFactory
import com.stefanbildl.FilterColumn
import com.stefanbildl.FilterInfo
import com.stefanbildl.FilterableReactiveTable
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.TableItem
import java.net.InetAddress

/**
 * Created by stefan on 09.07.17.
 */


fun main(args: Array<String>) {
    val compositeDisposable = CompositeDisposable()
    val display = Display()
    val shell = Shell(display).apply {
        layout = GridLayout(2, false)
        addDisposeListener {  compositeDisposable.dispose()}
    }
    val info = SoundTouchDiscoverer.create(InetAddress.getLocalHost()).discoverInformation().firstOrError().blockingGet()
    createMainTable(shell, info)
    shell.open()
    while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
            display.sleep()
        }
    }
}


fun createMainTable(shell: Shell, info: Info) {
    info.api!!.apply {
        val api = this
        val flowableFactory = object: FilterBasedFlowableFactory<Preset>() {
            override fun newFlowable(filterInfo: FilterInfo?): Flowable<Preset> =
                getPresets().toFlowable(BackpressureStrategy.ERROR).sorted {
                    o1, o2 ->
                    (filterInfo?:FilterInfo(true, "ID")).apply {
                        return@sorted when(this.column) {
                            "NAME"-> if(this.isAscending) (o1.contentItems.first().itemName?:"").compareTo(o2.contentItems.first().itemName?:"") else (o2.contentItems.first().itemName?:"").compareTo(o1.contentItems.first().itemName?:"")
                            else -> if(this.isAscending) (o1.id?:0).compareTo(o2.id?:0) else (o2.id?:0).compareTo(o1.id?:0)
                        }
                    }
                    return@sorted 1
                }
        }

        this.getPresets().subscribeOn(Schedulers.io()).observeOn(Schedulers.from(shell.display::syncExec)).toFlowable(BackpressureStrategy.ERROR).subscribeWith(object: FilterableReactiveTable<Preset>(shell, SWT.FULL_SELECTION or SWT.SINGLE, flowableFactory){
            override fun getColumns(): MutableCollection<out FilterColumn> = mutableListOf(FilterColumn("ID", "Preset ID", 200), FilterColumn("NAME", "Sender Name", 400))
            override fun fillItem(item: TableItem?, element: Preset?) {
                if(item != null && element != null)
                    item.apply { setText(arrayOf(element.id.toString(), element.contentItems.first().itemName)); data = element}
            }
        }.apply {
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
            table.addSelectionListener(object: SelectionAdapter(){
                override fun widgetSelected(e: SelectionEvent?) {
                    super.widgetSelected(e)
                    if(table.selectionCount > 0)
                        table.selection.forEach {
                            val preset = it.data
                            if(preset is Preset)
                                api.select(preset.contentItems[0]).blockingAwait()
                        }
                }
            })
        })
    }
}
