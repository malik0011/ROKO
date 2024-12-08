import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roko.MainActivity
import com.example.roko.R
import com.example.roko.utils.CompressedImageAdapter
import com.example.roko.utils.StorageHelper

class GalleryFragment : Fragment() {
    private var _recyclerView: RecyclerView? = null
    private val recyclerView get() = _recyclerView!!
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        _recyclerView = view.findViewById(R.id.rcv)
        setUpRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _recyclerView = null
    }

    fun setUpRecyclerView() {
        _recyclerView?.let { rv ->
            rv.layoutManager = LinearLayoutManager(context)
            val compressedImages = StorageHelper.getCompressedPhotosDirectory().listFiles()?.toList() ?: emptyList()
            val adapter = CompressedImageAdapter(compressedImages) { imageName ->
                mainActivity.decompressImage(imageName)
            }
            rv.adapter = adapter
        }
    }
} 