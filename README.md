# Back end

### Usage

Note: ensure that Maven and Docker are installed.

1. Run	

   ```bash
   mvn clean package
   ```

2. After that, run for build the docker image

   ```bash
   docker build --rm -t backend:latest .
   ```

3. Finally, run

   ```bash
   docker run --rm -d -p 8080:8080/tcp backend:latest
   ```

4. The API will be running at [localhost:8080](localhost:8080). Check Documentation section for endpoints.



# Documentation

The application was built using Java with Spring Boot to expose REST methods. For persists the data, the system uses Hibernate and JPA, with a H2 database.

## System description 

The TodoApp should offer the possibility to manage a set of tasks or todos. A Todo its represented by an ID field (provided by DB), a title, a description, an image, and a status field (which could be ```
                                                                                                                                                                                                       PENDING
                                                                                                                                                                                                       ``` or ```
                                                                                                                                                                                                              DONE                                                                                                                                                                                                              ```
. The user its allowed to CREATE, READ and MODIFY the Todos into the system.

## API

The REST API provides different endpoints (Table 1) to satisfy the above requirements, which are described below:

| Method | URL               | Brief description                                            |
| ------ | ----------------- | ------------------------------------------------------------ |
| POST   | /todo      | Creates a new Todo based in the required data. |
| GET    | /todo      | Retrieve all Todos stored in the system. It could be filtered by some params that will be described with more details in the followings sections. |
| PATCH  | /todo/{id} | Modify a Todo stored in the system associated at id parameter. It only allows to modify the status of the todo. |
*Table 1: summary of the REST API endpoints*

### POST
By a POST request, the user is able to create a new Todo. The user must give an image for create a new one, that image its stored in the server filesystem and that path its stored in the BD and the Todo entity stores a reference to that path. By this strategy, we have a better memory management than if we save the image directly on the database as a Blob field.

The status field, its automatically sets in `PENDING`.

#### URL
``` POST /todo```

#### Data Params
The data required by the system is sent into the body requests (`title`, `description`  and `image`) in  multipart/form-data format. All the parameters are mandatory fields, so if one of them is missing the application will return an appropriate error message.
- **Content example:** 
    `title = Buy food`
    `description = Buy meat, fruits and vegetables`
    `image = @D:\Users\User\Documents\Screenshots\2019-04-08 10_40_35-.png  `
#### Success Response
- **HTTP Status:** 201 CREATED

- **Content example:** 

  `{
      "error": null,
      "content": {
          "id": 1,
          "title": "Buy food",
          "description": "Buy meat, fruits and vegetables",
          "status": "PENDING",
          "path": "upload-dir/2ce3da4b-e2c3-4dc1-8111-0a1a20d57eb9.png"
      }
  }`
#### Error Response
An error response could be returned by the server if occurs an error at the image saving process. That response informs about the error.
- **HTTP Status:** 400 BAD REQUEST

- **Content example:** 

  `{
      "error": "Error saving image",
      "content": null
  }`

#### Example
`POST /todo`

`{
    "error": null,
    "content": {
        "id": 1,
        "title": "Buy food",
        "description": "Buy meat, fruits and vegetables",
        "status": "PENDING",
        "path": "upload-dir/2ce3da4b-e2c3-4dc1-8111-0a1a20d57eb9.png"
    }
}`	

### GET

Using a GET method, the system will return a list of all Todo's stored in the system. The system can filter the results if the user pass the query params for that. That filtering process could be done by id, or/and description, or/and status.

#### URL

``` GET /todo```

#### Query Params

`id` = An identifier associated to a Todo. **Optional parameter**

`description` = A set of words or letters which a todo description must have. **Optional parameter**

`status` = Its the desired todo status to filter, which could be `PENDING` or `DONE`. **Optional parameter**

#### Success Response

- **HTTP Status:** 200 OK

- **Content example:** 

  `{
      "error": null,
      "content": [
          {
              "id": 1,
              "title": "Buy food",
              "description": "Buy meat",
              "status": "PENDING",
              "path": "upload-dir/2cc44f91-c768-4890-8d5a-1dcc740b4a03.png"
          },
          {
              "id": 2,
              "title": "Pay bills",
              "description": "Pay bills online",
              "status": "DONE",
              "path": "upload-dir/48d5a3d2-e5da-4c1d-bbcf-be757299ef99.png"
          }
      ]
  }`

#### Example

- Filter by id

`GET /todo?id=1`

`{
    "error": null,
    "content": {
        "id": 1,
        "title": "Buy food",
        "description": "Buy meat, fruits and vegetables",
        "status": "PENDING",
        "path": "upload-dir/2ce3da4b-e2c3-4dc1-8111-0a1a20d57eb9.png"
    }
}`	

- Filter by description

`GET /todo?description=e`

`{
    "error": null,
    "content": [
        {
            "id": 1,
            "title": "Buy food",
            "description": "Buy meat",
            "status": "PENDING",
            "path": "upload-dir/2cc44f91-c768-4890-8d5a-1dcc740b4a03.png"
        },
        {
            "id": 2,
            "title": "Pay bills",
            "description": "Pay bills online",
            "status": "DONE",
            "path": "upload-dir/48d5a3d2-e5da-4c1d-bbcf-be757299ef99.png"
        }
    ]
}`

- Filter by status

`GET /todo?status=DONE`

`{
    "error": null,
    "content": [
        {
            "id": 2,
            "title": "Pay bills",
            "description": "Pay bills online",
            "status": "DONE",
            "path": "upload-dir/48d5a3d2-e5da-4c1d-bbcf-be757299ef99.png"
        }
    ]
}`

### PATCH

Using a PATCH request, the user is able to modify a existing Todo. Only can modify the Todo's status, setting it to `DONE`.

#### URL

`PATCH /todo/{id}`

#### Query Params

`id` = A identifier associated to a todo. **Mandatory parameter**.

#### Data Params

The data required to modify an existing Todo is sent into the body requests (status) in JSON format. Status its mandatory field, so if it is missing, the application will return an appropriate error message.

**Content example:**

`{
	"status": "DONE"
}`

#### Success Response

- **HTTP Status:** 200 OK
- **Content:**

`{
    "error": null,
    "content": 2
}`

#### Error Response

1. If occurs an error with the DB transaction:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:**

     `{
         "error": "Error updating todo",
         "content": null
     }`