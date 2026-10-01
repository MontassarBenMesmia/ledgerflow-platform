import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
export const options = { stages: [{ duration: '15s', target: 10 }, { duration: '30s', target: 10 }, { duration: '10s', target: 0 }], thresholds: { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<500'] } };
export function setup(){const response=http.post('http://localhost/realms/ledgerflow/protocol/openid-connect/token',{client_id:'ledgerflow-web',username:'operator',password:'operator123',grant_type:'password'});return{token:response.json('access_token')}}
export default function(data){const payload=JSON.stringify({sourceAccountId:'11111111-1111-1111-1111-111111111111',destinationAccountId:'22222222-2222-2222-2222-222222222222',amount:1,currency:'EUR'});const response=http.post('http://localhost/api/ledger/transfers',payload,{headers:{Authorization:`Bearer ${data.token}`,'Content-Type':'application/json','Idempotency-Key':uuidv4()}});check(response,{'transfer accepted':r=>r.status===201});sleep(.2)}
