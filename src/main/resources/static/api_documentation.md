# API Documentation

## AuthController

---
### authenticate

> BASIC

**Path:** /api/auth/authenticate

**Method:** POST


> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string |  |


**Request Demo:**

```json
{
  "username": "",
  "password": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| currentUser | string |  |
| accessToken | string |  |
| role | string |  |


**Response Demo:**

```json
{
  "currentUser": "",
  "accessToken": "",
  "role": ""
}
```

---
### signout

> BASIC

**Path:** /api/auth/signout

**Method:** POST


> REQUEST



> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| headers | map |  |
| body | object |  |
| status | object |  |


**Response Demo:**

```json
{
  "headers": {
    "": null
  },
  "body": {  },
  "status": {  }
}
```

## CustomerController

---
### createCustomer

> BASIC

**Path:** /api/customers

**Method:** POST


> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string | Only for creation/updates |
| email | string |  |
| roles | string[] |  |
| companyName | string |  |
| phone | string |  |


**Request Demo:**

```json
{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "companyName": "",
  "phone": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| companyName | string |  |
| phone | string |  |
| slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "companyName": "",
  "phone": "",
  "slaContractIds": [
    0
  ]
}
```

---
### getAllCustomers

> BASIC

**Path:** /api/customers

**Method:** GET


> REQUEST



> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| [0].id | long |  |
| [0].username | string |  |
| [0].email | string |  |
| [0].roles | string[] |  |
| [0].createdAt | string |  |
| [0].updatedAt | string |  |
| [0].companyName | string |  |
| [0].phone | string |  |
| [0].slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
[
  {
    "id": 0,
    "username": "",
    "email": "",
    "roles": [
      ""
    ],
    "createdAt": "",
    "updatedAt": "",
    "companyName": "",
    "phone": "",
    "slaContractIds": [
      0
    ]
  }
]
```

---
### getCustomerById

> BASIC

**Path:** /api/customers/{id}

**Method:** GET


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| companyName | string |  |
| phone | string |  |
| slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "companyName": "",
  "phone": "",
  "slaContractIds": [
    0
  ]
}
```

---
### updateCustomer

> BASIC

**Path:** /api/customers/{id}

**Method:** PUT


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string | Only for creation/updates |
| email | string |  |
| roles | string[] |  |
| companyName | string |  |
| phone | string |  |


**Request Demo:**

```json
{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "companyName": "",
  "phone": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| companyName | string |  |
| phone | string |  |
| slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "companyName": "",
  "phone": "",
  "slaContractIds": [
    0
  ]
}
```

---
### deleteCustomer

> BASIC

**Path:** /api/customers/{id}

**Method:** DELETE


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| headers | map |  |
| body | object |  |
| status | object |  |


**Response Demo:**

```json
{
  "headers": {
    "": null
  },
  "body": {  },
  "status": {  }
}
```

## POST /api/tickets → create ticket

---
### create

> BASIC

**Path:** /api/tickets

**Method:** POST


> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| title | string |  |
| description | string |  |
| customerId | long |  |
| slaContractId | long |  |
| assignedMemberId | long |  |


**Request Demo:**

```json
{
  "title": "",
  "description": "",
  "customerId": 0,
  "slaContractId": 0,
  "assignedMemberId": 0
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| title | string |  |
| description | string |  |
| status | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| customerId | long |  |
| slaContractId | long |  |
| assignedMemberId | long |  |
| createdAt | string |  |
| updatedAt | string |  |
| messages | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─senderId | long |  |
| &ensp;&ensp;&#124;─senderName | string |  |
| &ensp;&ensp;&#124;─message | string |  |
| &ensp;&ensp;&#124;─sentAt | string |  |
| attachments | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─fileName | string |  |
| &ensp;&ensp;&#124;─contentType | string |  |
| &ensp;&ensp;&#124;─size | long |  |
| &ensp;&ensp;&#124;─filePath | string |  |
| &ensp;&ensp;&#124;─uploadedById | long |  |
| &ensp;&ensp;&#124;─uploadedAt | string |  |
| statusHistory | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─oldStatus | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| &ensp;&ensp;&#124;─newStatus | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| &ensp;&ensp;&#124;─changedById | long |  |
| &ensp;&ensp;&#124;─changedByName | string |  |
| &ensp;&ensp;&#124;─note | string |  |
| &ensp;&ensp;&#124;─changedAt | string |  |


**Response Demo:**

```json
{
  "id": 0,
  "title": "",
  "description": "",
  "status": "",
  "customerId": 0,
  "slaContractId": 0,
  "assignedMemberId": 0,
  "createdAt": "",
  "updatedAt": "",
  "messages": [
    {
      "id": 0,
      "senderId": 0,
      "senderName": "",
      "message": "",
      "sentAt": ""
    }
  ],
  "attachments": [
    {
      "id": 0,
      "fileName": "",
      "contentType": "",
      "size": 0,
      "filePath": "",
      "uploadedById": 0,
      "uploadedAt": ""
    }
  ],
  "statusHistory": [
    {
      "id": 0,
      "oldStatus": "",
      "newStatus": "",
      "changedById": 0,
      "changedByName": "",
      "note": "",
      "changedAt": ""
    }
  ]
}
```

---
### getAll

> BASIC

**Path:** /api/tickets

**Method:** GET


> REQUEST



> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| [0].id | long |  |
| [0].title | string |  |
| [0].status | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| [0].customerId | long |  |
| [0].assignedMemberId | long |  |
| [0].createdAt | string |  |


**Response Demo:**

```json
[
  {
    "id": 0,
    "title": "",
    "status": "",
    "customerId": 0,
    "assignedMemberId": 0,
    "createdAt": ""
  }
]
```

---
### update

> BASIC

**Path:** /api/tickets/{ticketId}

**Method:** PUT


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| ticketId |  | NO |  |

**Query:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| actorId |  | NO |  |

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| title | string |  |
| description | string |  |
| slaContractId | long |  |
| assignedMemberId | long |  |
| status | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| statusNote | string | Optional human-readable note describing why the status was changed. |


**Request Demo:**

```json
{
  "title": "",
  "description": "",
  "slaContractId": 0,
  "assignedMemberId": 0,
  "status": "",
  "statusNote": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| title | string |  |
| description | string |  |
| status | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| customerId | long |  |
| slaContractId | long |  |
| assignedMemberId | long |  |
| createdAt | string |  |
| updatedAt | string |  |
| messages | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─senderId | long |  |
| &ensp;&ensp;&#124;─senderName | string |  |
| &ensp;&ensp;&#124;─message | string |  |
| &ensp;&ensp;&#124;─sentAt | string |  |
| attachments | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─fileName | string |  |
| &ensp;&ensp;&#124;─contentType | string |  |
| &ensp;&ensp;&#124;─size | long |  |
| &ensp;&ensp;&#124;─filePath | string |  |
| &ensp;&ensp;&#124;─uploadedById | long |  |
| &ensp;&ensp;&#124;─uploadedAt | string |  |
| statusHistory | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─oldStatus | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| &ensp;&ensp;&#124;─newStatus | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| &ensp;&ensp;&#124;─changedById | long |  |
| &ensp;&ensp;&#124;─changedByName | string |  |
| &ensp;&ensp;&#124;─note | string |  |
| &ensp;&ensp;&#124;─changedAt | string |  |


**Response Demo:**

```json
{
  "id": 0,
  "title": "",
  "description": "",
  "status": "",
  "customerId": 0,
  "slaContractId": 0,
  "assignedMemberId": 0,
  "createdAt": "",
  "updatedAt": "",
  "messages": [
    {
      "id": 0,
      "senderId": 0,
      "senderName": "",
      "message": "",
      "sentAt": ""
    }
  ],
  "attachments": [
    {
      "id": 0,
      "fileName": "",
      "contentType": "",
      "size": 0,
      "filePath": "",
      "uploadedById": 0,
      "uploadedAt": ""
    }
  ],
  "statusHistory": [
    {
      "id": 0,
      "oldStatus": "",
      "newStatus": "",
      "changedById": 0,
      "changedByName": "",
      "note": "",
      "changedAt": ""
    }
  ]
}
```

---
### getById

> BASIC

**Path:** /api/tickets/{ticketId}

**Method:** GET


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| ticketId |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| title | string |  |
| description | string |  |
| status | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| customerId | long |  |
| slaContractId | long |  |
| assignedMemberId | long |  |
| createdAt | string |  |
| updatedAt | string |  |
| messages | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─senderId | long |  |
| &ensp;&ensp;&#124;─senderName | string |  |
| &ensp;&ensp;&#124;─message | string |  |
| &ensp;&ensp;&#124;─sentAt | string |  |
| attachments | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─fileName | string |  |
| &ensp;&ensp;&#124;─contentType | string |  |
| &ensp;&ensp;&#124;─size | long |  |
| &ensp;&ensp;&#124;─filePath | string |  |
| &ensp;&ensp;&#124;─uploadedById | long |  |
| &ensp;&ensp;&#124;─uploadedAt | string |  |
| statusHistory | object[] |  |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─oldStatus | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| &ensp;&ensp;&#124;─newStatus | string | UNALLOCATED<br>ASSIGNED<br>IN_PROGRESS<br>CLOSED<br>RESOLVED |
| &ensp;&ensp;&#124;─changedById | long |  |
| &ensp;&ensp;&#124;─changedByName | string |  |
| &ensp;&ensp;&#124;─note | string |  |
| &ensp;&ensp;&#124;─changedAt | string |  |


**Response Demo:**

```json
{
  "id": 0,
  "title": "",
  "description": "",
  "status": "",
  "customerId": 0,
  "slaContractId": 0,
  "assignedMemberId": 0,
  "createdAt": "",
  "updatedAt": "",
  "messages": [
    {
      "id": 0,
      "senderId": 0,
      "senderName": "",
      "message": "",
      "sentAt": ""
    }
  ],
  "attachments": [
    {
      "id": 0,
      "fileName": "",
      "contentType": "",
      "size": 0,
      "filePath": "",
      "uploadedById": 0,
      "uploadedAt": ""
    }
  ],
  "statusHistory": [
    {
      "id": 0,
      "oldStatus": "",
      "newStatus": "",
      "changedById": 0,
      "changedByName": "",
      "note": "",
      "changedAt": ""
    }
  ]
}
```

---
### addMessage

> BASIC

**Path:** /api/tickets/{ticketId}/messages

**Method:** POST


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| ticketId |  | NO |  |

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| senderId | long |  |
| message | string |  |


**Request Demo:**

```json
{
  "senderId": 0,
  "message": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| senderId | long |  |
| senderName | string |  |
| message | string |  |
| sentAt | string |  |


**Response Demo:**

```json
{
  "id": 0,
  "senderId": 0,
  "senderName": "",
  "message": "",
  "sentAt": ""
}
```

## SlaContractController

---
### createSlaContract

> BASIC

**Path:** /api/sla-contracts

**Method:** POST


> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| contractName | string |  |
| serviceScope | string |  |
| responseTimeHours | int |  |
| isActive | boolean |  |
| customerId | long | Foreign key to Customer |


**Request Demo:**

```json
{
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "customerId": 0
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| contractName | string |  |
| serviceScope | string |  |
| responseTimeHours | int |  |
| isActive | boolean |  |
| createdAt | string |  |
| updatedAt | string |  |
| customer | object | Nested DTO to show customer details |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─username | string |  |
| &ensp;&ensp;&#124;─email | string |  |
| &ensp;&ensp;&#124;─roles | string[] |  |
| &ensp;&ensp;&#124;─createdAt | string |  |
| &ensp;&ensp;&#124;─updatedAt | string |  |
| &ensp;&ensp;&#124;─companyName | string |  |
| &ensp;&ensp;&#124;─phone | string |  |
| &ensp;&ensp;&#124;─slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
{
  "id": 0,
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "createdAt": "",
  "updatedAt": "",
  "customer": {
    "id": 0,
    "username": "",
    "email": "",
    "roles": [
      ""
    ],
    "createdAt": "",
    "updatedAt": "",
    "companyName": "",
    "phone": "",
    "slaContractIds": [
      0
    ]
  }
}
```

---
### getAllSlaContracts

> BASIC

**Path:** /api/sla-contracts

**Method:** GET


> REQUEST



> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| [0].id | long |  |
| [0].contractName | string |  |
| [0].serviceScope | string |  |
| [0].responseTimeHours | int |  |
| [0].isActive | boolean |  |
| [0].createdAt | string |  |
| [0].updatedAt | string |  |
| [0].customer | object | Nested DTO to show customer details |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─username | string |  |
| &ensp;&ensp;&#124;─email | string |  |
| &ensp;&ensp;&#124;─roles | string[] |  |
| &ensp;&ensp;&#124;─createdAt | string |  |
| &ensp;&ensp;&#124;─updatedAt | string |  |
| &ensp;&ensp;&#124;─companyName | string |  |
| &ensp;&ensp;&#124;─phone | string |  |
| &ensp;&ensp;&#124;─slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
[
  {
    "id": 0,
    "contractName": "",
    "serviceScope": "",
    "responseTimeHours": 0,
    "isActive": false,
    "createdAt": "",
    "updatedAt": "",
    "customer": {
      "id": 0,
      "username": "",
      "email": "",
      "roles": [
        ""
      ],
      "createdAt": "",
      "updatedAt": "",
      "companyName": "",
      "phone": "",
      "slaContractIds": [
        0
      ]
    }
  }
]
```

---
### getSlaContractById

> BASIC

**Path:** /api/sla-contracts/{id}

**Method:** GET


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| contractName | string |  |
| serviceScope | string |  |
| responseTimeHours | int |  |
| isActive | boolean |  |
| createdAt | string |  |
| updatedAt | string |  |
| customer | object | Nested DTO to show customer details |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─username | string |  |
| &ensp;&ensp;&#124;─email | string |  |
| &ensp;&ensp;&#124;─roles | string[] |  |
| &ensp;&ensp;&#124;─createdAt | string |  |
| &ensp;&ensp;&#124;─updatedAt | string |  |
| &ensp;&ensp;&#124;─companyName | string |  |
| &ensp;&ensp;&#124;─phone | string |  |
| &ensp;&ensp;&#124;─slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
{
  "id": 0,
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "createdAt": "",
  "updatedAt": "",
  "customer": {
    "id": 0,
    "username": "",
    "email": "",
    "roles": [
      ""
    ],
    "createdAt": "",
    "updatedAt": "",
    "companyName": "",
    "phone": "",
    "slaContractIds": [
      0
    ]
  }
}
```

---
### updateSlaContract

> BASIC

**Path:** /api/sla-contracts/{id}

**Method:** PUT


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| contractName | string |  |
| serviceScope | string |  |
| responseTimeHours | int |  |
| isActive | boolean |  |
| customerId | long | Foreign key to Customer |


**Request Demo:**

```json
{
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "customerId": 0
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| contractName | string |  |
| serviceScope | string |  |
| responseTimeHours | int |  |
| isActive | boolean |  |
| createdAt | string |  |
| updatedAt | string |  |
| customer | object | Nested DTO to show customer details |
| &ensp;&ensp;&#124;─id | long |  |
| &ensp;&ensp;&#124;─username | string |  |
| &ensp;&ensp;&#124;─email | string |  |
| &ensp;&ensp;&#124;─roles | string[] |  |
| &ensp;&ensp;&#124;─createdAt | string |  |
| &ensp;&ensp;&#124;─updatedAt | string |  |
| &ensp;&ensp;&#124;─companyName | string |  |
| &ensp;&ensp;&#124;─phone | string |  |
| &ensp;&ensp;&#124;─slaContractIds | long[] | Including IDs of contracts to avoid infinite recursion |


**Response Demo:**

```json
{
  "id": 0,
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "createdAt": "",
  "updatedAt": "",
  "customer": {
    "id": 0,
    "username": "",
    "email": "",
    "roles": [
      ""
    ],
    "createdAt": "",
    "updatedAt": "",
    "companyName": "",
    "phone": "",
    "slaContractIds": [
      0
    ]
  }
}
```

---
### deleteSlaContract

> BASIC

**Path:** /api/sla-contracts/{id}

**Method:** DELETE


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| headers | map |  |
| body | object |  |
| status | object |  |


**Response Demo:**

```json
{
  "headers": {
    "": null
  },
  "body": {  },
  "status": {  }
}
```

## TeamManagerController

---
### createTeamManager

> BASIC

**Path:** /api/team-managers

**Method:** POST


> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string |  |
| email | string |  |
| roles | string[] |  |
| department | string |  |


**Request Demo:**

```json
{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "department": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| department | string |  |
| teamMemberIds | long[] | List of team member IDs |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "department": "",
  "teamMemberIds": [
    0
  ]
}
```

---
### getAllTeamManagers

> BASIC

**Path:** /api/team-managers

**Method:** GET


> REQUEST



> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| [0].id | long |  |
| [0].username | string |  |
| [0].email | string |  |
| [0].roles | string[] |  |
| [0].createdAt | string |  |
| [0].updatedAt | string |  |
| [0].department | string |  |
| [0].teamMemberIds | long[] | List of team member IDs |


**Response Demo:**

```json
[
  {
    "id": 0,
    "username": "",
    "email": "",
    "roles": [
      ""
    ],
    "createdAt": "",
    "updatedAt": "",
    "department": "",
    "teamMemberIds": [
      0
    ]
  }
]
```

---
### getTeamManagerById

> BASIC

**Path:** /api/team-managers/{id}

**Method:** GET


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| department | string |  |
| teamMemberIds | long[] | List of team member IDs |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "department": "",
  "teamMemberIds": [
    0
  ]
}
```

---
### updateTeamManager

> BASIC

**Path:** /api/team-managers/{id}

**Method:** PUT


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string |  |
| email | string |  |
| roles | string[] |  |
| department | string |  |


**Request Demo:**

```json
{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "department": ""
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| department | string |  |
| teamMemberIds | long[] | List of team member IDs |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "department": "",
  "teamMemberIds": [
    0
  ]
}
```

---
### deleteTeamManager

> BASIC

**Path:** /api/team-managers/{id}

**Method:** DELETE


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| headers | map |  |
| body | object |  |
| status | object |  |


**Response Demo:**

```json
{
  "headers": {
    "": null
  },
  "body": {  },
  "status": {  }
}
```

## TeamMemberController

---
### createTeamMember

> BASIC

**Path:** /api/team-members

**Method:** POST


> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string |  |
| email | string |  |
| roles | string[] |  |
| availabilityStatus | string | AVAILABLE<br>BUSY<br>OFF_DUTY<br>UNAVAILABLE |
| jobTitle | string |  |
| managerId | long | ID of the TeamManager |


**Request Demo:**

```json
{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| availabilityStatus | string | AVAILABLE<br>BUSY<br>OFF_DUTY<br>UNAVAILABLE |
| jobTitle | string |  |
| managerId | long | Reference to manager by ID to avoid recursion |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}
```

---
### getAllTeamMembers

> BASIC

**Path:** /api/team-members

**Method:** GET


> REQUEST



> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| [0].id | long |  |
| [0].username | string |  |
| [0].email | string |  |
| [0].roles | string[] |  |
| [0].createdAt | string |  |
| [0].updatedAt | string |  |
| [0].availabilityStatus | string | AVAILABLE<br>BUSY<br>OFF_DUTY<br>UNAVAILABLE |
| [0].jobTitle | string |  |
| [0].managerId | long | Reference to manager by ID to avoid recursion |


**Response Demo:**

```json
[
  {
    "id": 0,
    "username": "",
    "email": "",
    "roles": [
      ""
    ],
    "createdAt": "",
    "updatedAt": "",
    "availabilityStatus": "",
    "jobTitle": "",
    "managerId": 0
  }
]
```

---
### getTeamMemberById

> BASIC

**Path:** /api/team-members/{id}

**Method:** GET


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| availabilityStatus | string | AVAILABLE<br>BUSY<br>OFF_DUTY<br>UNAVAILABLE |
| jobTitle | string |  |
| managerId | long | Reference to manager by ID to avoid recursion |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}
```

---
### updateTeamMember

> BASIC

**Path:** /api/team-members/{id}

**Method:** PUT


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | NO |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string |  |
| password | string |  |
| email | string |  |
| roles | string[] |  |
| availabilityStatus | string | AVAILABLE<br>BUSY<br>OFF_DUTY<br>UNAVAILABLE |
| jobTitle | string |  |
| managerId | long | ID of the TeamManager |


**Request Demo:**

```json
{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}
```

> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| id | long |  |
| username | string |  |
| email | string |  |
| roles | string[] |  |
| createdAt | string |  |
| updatedAt | string |  |
| availabilityStatus | string | AVAILABLE<br>BUSY<br>OFF_DUTY<br>UNAVAILABLE |
| jobTitle | string |  |
| managerId | long | Reference to manager by ID to avoid recursion |


**Response Demo:**

```json
{
  "id": 0,
  "username": "",
  "email": "",
  "roles": [
    ""
  ],
  "createdAt": "",
  "updatedAt": "",
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}
```

---
### deleteTeamMember

> BASIC

**Path:** /api/team-members/{id}

**Method:** DELETE


> REQUEST

**Path Params:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| id |  | NO |  |


> RESPONSE

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| headers | map |  |
| body | object |  |
| status | object |  |


**Response Demo:**

```json
{
  "headers": {
    "": null
  },
  "body": {  },
  "status": {  }
}
```
