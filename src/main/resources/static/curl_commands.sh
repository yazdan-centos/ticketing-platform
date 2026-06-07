## authenticate
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": ""
}' 'http://localhost:8080/api/auth/authenticate'
```

---

## signout
```bash
curl -X POST 'http://localhost:8080/api/auth/signout'
```

---

## listPermissions
```bash
curl -X GET 'http://localhost:8080/api/admin/access/permissions'
```

---

## getEffectiveAccess
```bash
curl -X GET 'http://localhost:8080/api/admin/access/users/{userId}'
```

---

## listGrants
```bash
curl -X GET 'http://localhost:8080/api/admin/access/users/{userId}/grants'
```

---

## upsertGrant
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "permissionCode": "",
  "effect": ""
}' 'http://localhost:8080/api/admin/access/users/{userId}/grants'
```

---

## removeGrant
```bash
curl -X DELETE 'http://localhost:8080/api/admin/access/users/{userId}/grants/{permissionCode}'
```

---

## listScopes
```bash
curl -X GET 'http://localhost:8080/api/admin/access/users/{userId}/scopes'
```

---

## setScope
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "scope": ""
}' 'http://localhost:8080/api/admin/access/users/{userId}/scopes/{resourceType}'
```

---

## clearScope
```bash
curl -X DELETE 'http://localhost:8080/api/admin/access/users/{userId}/scopes/{resourceType}'
```

---

## listRolePermissions
```bash
curl -X GET 'http://localhost:8080/api/admin/access/roles/{roleName}/permissions'
```

---

## replaceRolePermissions
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "permissionCodes": [
    ""
  ]
}' 'http://localhost:8080/api/admin/access/roles/{roleName}/permissions'
```

---

## create
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "title": "",
  "description": "",
  "customerId": 0,
  "slaContractId": 0,
  "assignedMemberId": 0
}' 'http://localhost:8080/api/tickets'
```

---

## update
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "title": "",
  "description": "",
  "slaContractId": 0,
  "assignedMemberId": 0,
  "status": "",
  "statusNote": ""
}' 'http://localhost:8080/api/tickets/{ticketId}?actorId='
```

---

## getById
```bash
curl -X GET 'http://localhost:8080/api/tickets/{ticketId}'
```

---

## getAll
```bash
curl -X GET 'http://localhost:8080/api/tickets'
```

---

## addMessage
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "senderId": 0,
  "message": ""
}' 'http://localhost:8080/api/tickets/{ticketId}/messages'
```

---

## createCustomer
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "companyName": "",
  "phone": ""
}' 'http://localhost:8080/api/customers'
```

---

## searchCustomers
```bash
curl -X GET 'http://localhost:8080/api/customers/search?firstName=&lastName=&username=&email=&companyName=&phone=&deleted=&pageable='
```

---

## getAllCustomers
```bash
curl -X GET 'http://localhost:8080/api/customers'
```

---

## getCustomerById
```bash
curl -X GET 'http://localhost:8080/api/customers/{id}'
```

---

## updateCustomer
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "companyName": "",
  "phone": ""
}' 'http://localhost:8080/api/customers/{id}'
```

---

## deleteCustomer
```bash
curl -X DELETE 'http://localhost:8080/api/customers/{id}'
```

---

## createTeamMember
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}' 'http://localhost:8080/api/team-members'
```

---

## getAllTeamMembers
```bash
curl -X GET 'http://localhost:8080/api/team-members'
```

---

## getTeamMemberById
```bash
curl -X GET 'http://localhost:8080/api/team-members/{id}'
```

---

## updateTeamMember
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "availabilityStatus": "",
  "jobTitle": "",
  "managerId": 0
}' 'http://localhost:8080/api/team-members/{id}'
```

---

## deleteTeamMember
```bash
curl -X DELETE 'http://localhost:8080/api/team-members/{id}'
```

---

## createSlaContract
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "customerId": 0
}' 'http://localhost:8080/api/sla-contracts'
```

---

## getAllSlaContracts
```bash
curl -X GET 'http://localhost:8080/api/sla-contracts'
```

---

## getSlaContractById
```bash
curl -X GET 'http://localhost:8080/api/sla-contracts/{id}'
```

---

## updateSlaContract
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "customerId": 0
}' 'http://localhost:8080/api/sla-contracts/{id}'
```

---

## deleteSlaContract
```bash
curl -X DELETE 'http://localhost:8080/api/sla-contracts/{id}'
```

---

## createTeamManager
```bash
curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "department": ""
}' 'http://localhost:8080/api/team-managers'
```

---

## getAllTeamManagers
```bash
curl -X GET 'http://localhost:8080/api/team-managers'
```

---

## getTeamManagerById
```bash
curl -X GET 'http://localhost:8080/api/team-managers/{id}'
```

---

## updateTeamManager
```bash
curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "department": ""
}' 'http://localhost:8080/api/team-managers/{id}'
```

---

## deleteTeamManager
```bash
curl -X DELETE 'http://localhost:8080/api/team-managers/{id}'
```