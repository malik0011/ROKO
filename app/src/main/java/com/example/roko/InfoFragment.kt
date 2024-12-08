import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.roko.R
import com.example.roko.utils.StorageHelper
import java.io.File

class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvVersion: TextView = view.findViewById(R.id.tvVersion)
        val tvOriginalPath: TextView = view.findViewById(R.id.tvOriginalPath)
        val tvCompressedPath: TextView = view.findViewById(R.id.tvCompressedPath)
        val btnOpenOriginal: Button = view.findViewById(R.id.btnOpenOriginal)
        val btnOpenCompressed: Button = view.findViewById(R.id.btnOpenCompressed)

        // Get app version
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        tvVersion.text = "Version ${packageInfo.versionName} (${packageInfo.versionCode})"

        // Get storage paths
        val originalPath = StorageHelper.getOriginalPhotosDirectory().absolutePath
        val compressedPath = StorageHelper.getCompressedPhotosDirectory().absolutePath

        tvOriginalPath.text = "Original Photos:\n$originalPath"
        tvCompressedPath.text = "Compressed Photos:\n$compressedPath"

        // Set up button click listeners
        btnOpenOriginal.setOnClickListener {
            openFolder(File(originalPath))
        }

        btnOpenCompressed.setOnClickListener {
            openFolder(File(compressedPath))
        }
    }

    private fun openFolder(folder: File) {
        try {
            // Create content URI using FileProvider
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                folder
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "resource/folder")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open folder with..."))
        } catch (e: Exception) {
            Toast.makeText(context, 
                "Cannot open folder: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }
} 