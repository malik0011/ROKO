import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roko.MainActivity
import com.example.roko.R
import com.example.roko.Utils.CompressedImageAdapter
import java.io.File

class GalleryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
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
        recyclerView = view.findViewById(R.id.rcv)
        setUpRecyclerView()
    }

    fun setUpRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        val compressedFolder = File(requireContext().filesDir, "CompressedPhotos")
        val compressedImages = compressedFolder.listFiles()?.toList() ?: emptyList()
        val adapter = CompressedImageAdapter(compressedImages) { imageName ->
            mainActivity.decompressImage(imageName)
        }
        recyclerView.adapter = adapter
    }
} 