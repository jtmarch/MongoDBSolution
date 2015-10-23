package course;

import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import course.model.Comment;
import course.model.Post;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogPostDAO {
    MongoCollection<Document> postsCollection;

    Datastore ds;

    public BlogPostDAO(final MongoDatabase blogDatabase, final Datastore datastore) {
        postsCollection = blogDatabase.getCollection("posts");
        ds = datastore;
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {

        // XXX HW 3.2,  Work Here
        Document post = new Document("permalink", permalink);
        post = postsCollection.find(post).first();

        return post;
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Post> findByDateDescending(int limit) {

        // XXX HW 3.2,  Work Here
        // Return a list of DBObjects, each one a post from the posts collection
        List<Document> posts = postsCollection.find().limit(limit).sort(new Document("date", -1)).into(new ArrayList<Document>());

        Query<Post> postCollection = ds.createQuery(Post.class).limit(limit).order("-date");

        return postCollection.asList();
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();


        // XXX HW 3.2, Work Here
        // Remember that a valid post has the following keys:
        // author, body, permalink, tags, comments, date
        //
        // A few hints:
        // - Don't forget to create an empty list of comments
        // - for the value of the date key, today's datetime is fine.
        // - tags are already in list form that implements suitable interface.
        // - we created the permalink for you above.

        // Build the post object and insert it
        Document post = new Document();
        post.append("title", title)
                .append("body", body)
                .append("tags", tags)
                .append("author", username)
                .append("permalink", permalink)
                .append("comments", new ArrayList<String>())
                .append("date", new Date());
        //postsCollection.insertOne(post);


        Post postObj = new Post();
        postObj.setTitle(title);
        postObj.setDate(new Date());
        postObj.setAuthor(username);
        postObj.setBodyContent(body);
        postObj.setPermalink(permalink);
        postObj.setComments(new ArrayList<Comment>());
        postObj.setTags(tags);
        ds.save(postObj);

        return permalink;
    }




    // White space to protect the innocent








    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {

        // XXX HW 3.3, Work Here
        // Hints:
        // - email is optional and may come in NULL. Check for that.
        // - best solution uses an update command to the database and a suitable
        //   operator to append the comment on to any existing list of comments
        Document comment = new Document();
        comment.put("author", name);
        comment.put("email", email);
        comment.put("body", body);

        Document update = new Document("$push", new Document("comments", comment));

        //postsCollection.updateOne(new Document("permalink", permalink), update);

        Comment commentObj = new Comment();
        commentObj.setAuthor(name);
        commentObj.setEmail(email);
        commentObj.setBody(body);

        Query<Post> updateQuery = ds.createQuery(Post.class).field("permalink").equal(permalink);
        UpdateOperations<Post> ops = ds.createUpdateOperations(Post.class).add("comments", commentObj);
        ds.update(updateQuery, ops);

    }
}
