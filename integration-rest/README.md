# Integration REST API

Initial implementation of a REST API. Currently, only basic functionality is available.

Please refer to the **Integration module** for detailed setup instructions.

---

## **GET Requests**

- **Get all routes** - `GET http://localhost:9999/configuration/routes`
- **Get a single route** - `GET http://localhost:9999/configuration/route/{id}`
- **Get all components** - `GET http://localhost:9999/configuration/components`
- **Get a single component** - `GET http://localhost:9999/configuration/component/{id}`

---

## **POST Requests**

- **Prevent a component receiving messages** - `POST http://localhost:9999/configuration/component/{id}/stop/inbound`
- **Prevent a component forwarding messages** - `POST http://localhost:9999/configuration/component/{id}/stop/outbound`
- **Allow a component to receive messages** - `POST http://localhost:9999/configuration/component/{id}/start/inbound`
- **Allow a component to forward messages** - `POST http://localhost:9999/configuration/component/{id}/start/outbound`
