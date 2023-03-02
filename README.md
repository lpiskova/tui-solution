# TUI Homework API

## REST API to manage Github repositories

The application provides the HTTP endpoint which accepts username and header “Accept: application/json”. 

```
GET /users/{username}/repositories
```

The endpoint returns all Github repositories of the given user which are not forks.
The response contains:
* Repository name
* Owner login
* For each branch it’s name and last commit sha

### Config

Make changes in resources/application.yaml if needed.

### Run
* Run the application by using `com.tuigroup.tuihomework.TuiHomeworkApplication`. 
* The application can also be run in a Docker container. The Docker image can be built as follows:

```
  1. mvn clean package
  2. docker build .
```

### Test

There are 2 kinds of tests:

* Unit tests
* Integration tests

### Documentation

The swagger documentation:

* [Local deployment](http://localhost:8080/swagger-ui/index.html)
* [Local deployment - yaml file](http://localhost:8080/api-docs.yaml)
