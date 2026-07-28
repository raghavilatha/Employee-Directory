let employees=[];
fetch('employees.json').then(r=>r.json()).then(data=>{
 employees=data;render(data);
 document.getElementById('search').addEventListener('input',e=>{
 const q=e.target.value.toLowerCase();
 render(employees.filter(x=>x.name.toLowerCase().includes(q)||x.department.toLowerCase().includes(q)));
 });
});
function render(rows){
 document.querySelector('#tbl tbody').innerHTML=
 rows.map(r=>`<tr><td>${r.id}</td><td>${r.name}</td><td>${r.department}</td><td>${r.status}</td></tr>`).join('');
}
