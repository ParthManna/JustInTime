import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.todolistapp.R
import java.time.LocalDate

class BlankFragment1 : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var adapter: InfiniteViewPageAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blank1, container, false)

        viewPager = view.findViewById(R.id.viewpage)
        adapter = InfiniteViewPageAdapter(childFragmentManager)

        // Initialize adapter with a wide range of months
        adapter.setBaseDate(LocalDate.now())

        viewPager.adapter = adapter
        viewPager.currentItem = adapter.getMiddlePosition() // Correct usage of 'currentItem'


        return view
    }
}
