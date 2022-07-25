# MovieTracker

An application for movie tracking that uses [themoviedb](https://www.themoviedb.org/) API. The user can see movie details, search, bookmark movies, find on the map other people that watched a certain movie and chat with them.

## Log in/ Register

The user can log in or register using a Google account, or the traditional method: e-mail and password. This was implemented using firebase authentification. Both the e-mail and the password use a pattern so the user cannot input unfitting information.

![image](https://drive.google.com/uc?export=view&id=12L-k6ZxFduFqu1ID0gWIWnlVpvfN7c7v)

Also, if you log out of your account, the e-mail and image link is being saved in shared preferences and auto completed on the log-in screen. When the e-mail in the field matches the saved e-mail, the image displayed changes from the default one to the personal one.

![image](https://drive.google.com/uc?export=view&id=1zEntciqbj-yzMZEYs3WPhiXKUAiwdkP_)

## The bottom navigation screens

### Popular movies screen

Here you can see the most recent and popular movies, and also search for some other movies. The page can be infinitely scrolled through, due to the use of pagination. You can also add and remove bookmark from a movie.

![image](https://drive.google.com/uc?export=view&id=1xb1R3BJTumdDGLth58zjY8xx9EBSXuUp)

### Top movies screen

Here you can see the highest rated moves. Similar to the popular movies screen.

![image](https://drive.google.com/uc?export=view&id=1ouLY44UwExCyn9OBLC-u19DuizN8-h9C)

### Saved movies

You can save movies and keep them in a list by clicking the bookmark icon for later reference.

![image](https://drive.google.com/uc?export=view&id=1u6d-FWe0JC6qxNO1fLzRvr3xhbutnalJ)

### Profile

You can access your profile to change your password or personalize it by adding a picture.

When changing your password, the old password is needed and the new password should not be the same and follow the password pattern.

![image](https://drive.google.com/uc?export=view&id=1E7BHKInPk5tP2mm04IUb7DxxPuURnPw5)

After either uploading or taking a picture, you can crop the picture and see it before saving the changes.
This feature was implemented using [a library](https://github.com/edmodo/cropper).

![image](https://drive.google.com/uc?export=view&id=1OAYq1s9oPwBTcsN404zJcritjIdKN23H)


## Other features

### Movie details and viewers

By tapping on any movie, you are redirected to a screen where more details are presented. There, you can see other people that watched the movie either in a list or a map. If no one watched the movie, accessing movie viewers is not possible (the button disappears). 

In the map you can set your own location and check other's location.

![image](https://drive.google.com/uc?export=view&id=1AYJUm-5a3lpouGyUO1aQtWykZqCGjh2S)

By tapping on another user's marker, you can see their details and open a chat window.

![image](https://drive.google.com/uc?export=view&id=1Kv3ayCi5TCbqJyNHmtw1JTXv0CCpVPuy)

### Chatting with users

You can send messages and pictures to other users, and they receive the message immediately. Tapping an image opens it in full screen.

![image](https://drive.google.com/uc?export=view&id=15nHZUJQh9r9h8KjPfDehCPqdMWGQdi4K)

### Deleting a conversation

By swiping right a conversation, you can delete it.

![image](https://drive.google.com/uc?export=view&id=1QmJBGEOFxmEWpmxpaxVraC-zLxBNw-vU)


All images were loaded using [Glide](https://github.com/bumptech/glide).

Dependency injection with Hilt was used in order to provide Retrofit; MovieApi and FirebaseService (internal classes).

