REST Editor Client Contrib
==========================
Plugin detail page: https://plugins.jetbrains.com/plugin/10369-rest-editor-client-contrib

# Features

* Generate rst file from Swagger json
* Generate rst file for Spring MVC Controller
* Generate rst file from Swagger API
* Connect method with related action in rst file

# Code Generation

Please create  rest-client.env.json in your project directory with following code:

```json
{
  "local": {
    "host": "http://localhost:8080"
  }
}
```

* Spring Controller class & action method

![](.README_images/generate_controller_request.png)

* Swagger Json: @Api class or @ApiOperation method

![](.README_images/generate_swagger_rest.png)

# Navigation

* Refer handler

![](.README_images/java_method_refer.png)

```
###@see #help
###@see ClassName#welcome 
 
```

* Navigate to http request

![Navigate To Http Request](.README_images/navigate_to_http_request.png)


# Live Templates

* post.form: Http form post
* post.upload: Http Upload
* post.stream: Stream upload
