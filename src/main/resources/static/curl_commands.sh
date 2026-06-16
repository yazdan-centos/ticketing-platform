## authenticate

curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": ""
}' 'http://localhost:8080/api/auth/authenticate'




## signout

curl -X POST 'http://localhost:8080/api/auth/signout'




## listPermissions

curl -X GET 'http://localhost:8080/api/admin/access/permissions'




## getEffectiveAccess

curl -X GET 'http://localhost:8080/api/admin/access/users/{userId}'




## listGrants

curl -X GET 'http://localhost:8080/api/admin/access/users/{userId}/grants'




## upsertGrant

curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "permissionCode": "",
  "effect": ""
}' 'http://localhost:8080/api/admin/access/users/{userId}/grants'




## removeGrant

curl -X DELETE 'http://localhost:8080/api/admin/access/users/{userId}/grants/{permissionCode}'




## listScopes

curl -X GET 'http://localhost:8080/api/admin/access/users/{userId}/scopes'




## setScope

curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "scope": ""
}' 'http://localhost:8080/api/admin/access/users/{userId}/scopes/{resourceType}'




## clearScope

curl -X DELETE 'http://localhost:8080/api/admin/access/users/{userId}/scopes/{resourceType}'




## listRolePermissions

curl -X GET 'http://localhost:8080/api/admin/access/roles/{roleName}/permissions'




## replaceRolePermissions

curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "permissionCodes": [
    ""
  ]
}' 'http://localhost:8080/api/admin/access/roles/{roleName}/permissions'




## create

curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "title": "",
  "description": "",
  "customerId": 0,
  "slaContractId": 0,
  "assignedMemberId": 0
}' 'http://localhost:8080/api/tickets'




## update

curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "title": "",
  "description": "",
  "slaContractId": 0,
  "assignedMemberId": 0,
  "status": "",
  "statusNote": ""
}' 'http://localhost:8080/api/tickets/{ticketId}?actorId='




## getById

curl -X GET 'http://localhost:8080/api/tickets/{ticketId}'




## getAll

curl -X GET 'http://localhost:8080/api/tickets'




## addMessage

curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "senderId": 0,
  "message": ""
}' 'http://localhost:8080/api/tickets/{ticketId}/messages'




## createCustomer

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




## searchCustomers

curl -X GET 'http://localhost:8080/api/customers/search?firstName=&lastName=&username=&email=&companyName=&phone=&deleted=&pageable='




## getAllCustomers

curl -X GET 'http://localhost:8080/api/customers'




## getCustomerById

curl -X GET 'http://localhost:8080/api/customers/{id}'




## updateCustomer

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




## deleteCustomer

curl -X DELETE 'http://localhost:8080/api/customers/{id}'




## createTeamMember

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




## getAllTeamMembers

curl -X GET 'http://localhost:8080/api/team-members'




## getTeamMemberById

curl -X GET 'http://localhost:8080/api/team-members/{id}'




## updateTeamMember

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




## deleteTeamMember

curl -X DELETE 'http://localhost:8080/api/team-members/{id}'




## createSlaContract

curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "customerId": 0
}' 'http://localhost:8080/api/sla-contracts'




## getAllSlaContracts

curl -X GET 'http://localhost:8080/api/sla-contracts'




## getSlaContractById

curl -X GET 'http://localhost:8080/api/sla-contracts/{id}'




## updateSlaContract

curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "contractName": "",
  "serviceScope": "",
  "responseTimeHours": 0,
  "isActive": false,
  "customerId": 0
}' 'http://localhost:8080/api/sla-contracts/{id}'




## deleteSlaContract

curl -X DELETE 'http://localhost:8080/api/sla-contracts/{id}'




## createTeamManager

curl -X POST -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "department": ""
}' 'http://localhost:8080/api/team-managers'




## getAllTeamManagers

curl -X GET 'http://localhost:8080/api/team-managers'




## getTeamManagerById

curl -X GET 'http://localhost:8080/api/team-managers/{id}'




## updateTeamManager

curl -X PUT -H 'Content-Type: application/json' -H 'Content-Type: application/json' -d '{
  "username": "",
  "password": "",
  "email": "",
  "roles": [
    ""
  ],
  "department": ""
}' 'http://localhost:8080/api/team-managers/{id}'




## deleteTeamManager

curl -X DELETE 'http://localhost:8080/api/team-managers/{id}'
