CREATE TABLE shibpid (
	localEntity VARCHAR(50) NOT NULL, 
	peerEntity VARCHAR(50) NOT NULL,
	principalName VARCHAR(50) NOT NULL, 
	localId VARCHAR(50) NOT NULL, 
	persistentId VARCHAR(50) NOT NULL, 
	peerProvidedId VARCHAR(50), 
	creationDate TIMESTAMP NOT NULL, 
	deactivationDate TIMESTAMP);
