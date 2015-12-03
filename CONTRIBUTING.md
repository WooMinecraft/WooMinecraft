# Contribution Guidelines

## WordPress Plugin
If you are not an owner, and would like to contribute to the WordPress plugin please follow these guidelines:
* Ensure all your code follows the [WordPress Coding Standards](https://codex.wordpress.org/WordPress_Coding_Standards).
* If working on an Issue, please [reference the issue in your commits](https://github.com/blog/957-introducing-issue-mentions).
* I18n all the things! [I18n from the codex](https://codex.wordpress.org/I18n_for_WordPress_Developers)

### Pull Requests
All Pull requests should be on the [dev branch](https://github.com/WooMinecraft/woominecraft-wp/tree/dev).

## Java Contributions
If you are contributing on the Java side of things please follow these guidelines:
* To ensure everything looks on point and because TekkitCommando is OCD please format code as show below:
```
public class Main {

    public static void main(String[] args) {
        System.out.println("This is an example");
    }
}
```
#### NOT
```
public class Main 
{
    public static void main(String[] args) 
    {
        System.out.println("This is an example");
    }
}
```
* If your code includes messages for the user such as warnings or info messages make sure that you have a translation for English and Spanish. (Use google translate if you don't know Spanish or vice versa)
* If working on an Issue, please [reference the issue in your commits](https://github.com/blog/957-introducing-issue-mentions).
