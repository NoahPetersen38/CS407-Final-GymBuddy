package com.cs407.gymsocialapp.sampledata

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import com.cs407.gymsocialapp.R
import java.util.Date

// define User entity
@Entity(
    indices = [Index(value = ["userName"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0, // auto-generated primary key for user
    val userName: String = "", // unique username from index
    val passwordHash: String, // hashed password
    val joinDate: Date // date when the user joined - start calendar
)

// define Post entity
@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class, // reference to User entity
        parentColumns = ["userId"], // column in User
        childColumns = ["userId"], // column in Post referencing User
        onDelete = ForeignKey.CASCADE // delete posts when user is deleted
    )],
    indices = [Index(value = ["userId"])]
)
data class Post(
    @PrimaryKey(autoGenerate = true) val postId: Int = 0, // auto-generated primary key for post
    val userId: Int, // foreign key referencing User
    val content: String, // content of the post -- i don't think this simple
    val timestamp: Date // time the post was created - for calendar
)

// define FollowRelation entity for many-to-many relationship between Users
@Entity(
    primaryKeys = ["followerId", "followeeId"], // composite primary key
    foreignKeys = [
        ForeignKey(
            entity = User::class, // foreign key referencing User
            parentColumns = ["userId"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE // cascade delete if user is deleted
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["followeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["followerId"]), Index(value = ["followeeId"])]
)
data class FollowRelation(
    val followerId: Int, // user following another user
    val followeeId: Int // user being followed
)

// converter class to handle conversion between custom type Date and SQL-compatible type Long
class Converters {
    @TypeConverter
    // converts a timestamp (Long) to a Date object
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    // converts a Date object to a timestamp (Long)
    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}

// DAO for users
@Dao
interface UserDao {
    // Query to get a User by their userName
    @Query("SELECT * FROM User WHERE userName = :name LIMIT 1")
    suspend fun getByName(name: String): User

    // Query to get a User by their userId
    @Query("SELECT * FROM User WHERE userId = :id")
    suspend fun getById(id: Int): User

    // Insert a new User
    @Insert
    suspend fun insert(user: User)
}

// DAO for posting from a user
@Dao
interface PostDao {
    // Query to get all posts by a user
    @Query("SELECT * FROM Post WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getPostsByUser(userId: Int): List<Post>

    // Query to get feed posts (posts by followed users)
    @Query(
        """SELECT * FROM Post WHERE userId IN (
            SELECT followeeId FROM FollowRelation WHERE followerId = :userId
        ) ORDER BY timestamp DESC"""
    )
    suspend fun getFeedForUser(userId: Int): List<Post>

    // Insert a new Post
    @Insert
    suspend fun insert(post: Post)
}

// DAO for following and folloee's of a User
@Dao
interface FollowRelationDao {
    // Add a follow relation
    @Insert
    suspend fun insert(relation: FollowRelation)

    // Query to get users followed by a user
    @Query("SELECT followeeId FROM FollowRelation WHERE followerId = :userId")
    suspend fun getFollowedUsers(userId: Int): List<Int>

    // Query to get followers of a user
    @Query("SELECT followerId FROM FollowRelation WHERE followeeId = :userId")
    suspend fun getFollowers(userId: Int): List<Int>
}

// Database class with all entities and DAOs
@Database(entities = [User::class, Post::class, FollowRelation::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // provide DAOs to access the database
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun followRelationDao(): FollowRelationDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Get or create the database instance
        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase", // Reference to a string resource
                ).build()
                INSTANCE = instance
                //return instance
                instance
            }
        }
    }
}