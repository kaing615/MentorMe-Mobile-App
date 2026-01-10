@echo off
echo Stopping any existing MongoDB processes...
taskkill /F /IM mongod.exe 2>nul

echo Creating data directory if not exists...
if not exist "C:\mongo-rs\rs1" mkdir "C:\mongo-rs\rs1"

echo Starting MongoDB replica set...
start "MongoDB ReplicaSet" mongod --replSet rs0 --port 27017 --dbpath "C:\mongo-rs\rs1" --bind_ip localhost

echo Waiting for MongoDB to start (10 seconds)...
timeout /t 10 /nobreak

echo Initializing replica set...
mongosh --eval "try { var status = rs.status(); if (status.ok) { print('Replica set already initialized'); } } catch(e) { if (e.codeName === 'NoReplicationEnabled' || e.message.includes('replSet')) { rs.initiate({ _id: 'rs0', members: [{ _id: 0, host: 'localhost:27017' }] }); print('Replica set initialized successfully'); } else { print('Error: ' + e); } }"

echo.
echo MongoDB is ready!
echo Press any key to close this window...
pause