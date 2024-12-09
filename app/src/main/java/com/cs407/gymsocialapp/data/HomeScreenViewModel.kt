import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.cs407.gymsocialapp.data.Post
import com.cs407.gymsocialapp.data.PostDao
import com.cs407.gymsocialapp.data.PostDatabase
import com.cs407.gymsocialapp.data.User
import com.cs407.gymsocialapp.data.UserDao
import kotlinx.coroutines.launch

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PostDatabase.getInstance(application)
    private val userDao = database.userDao()
    private val postDao = database.postDao()

    val allPosts: LiveData<List<Post>> = liveData {
        emit(postDao.getAllPosts())
    }

    fun getUser(userId: Int): LiveData<User?> = liveData {
        emit(userDao.getUserById(userId))
    }
}

