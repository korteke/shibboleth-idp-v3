CREATE TABLE shibpid (
	localEntity VARCHAR(255) NOT NULL, 
	peerEntity VARCHAR(255) NOT NULL,
    persistentId VARCHAR(50) NOT NULL, 
	principalName VARCHAR(50) NOT NULL, 
	localId VARCHAR(50) NOT NULL, 
	peerProvidedId VARCHAR(50) NULL, 
	creationDate TIMESTAMP NOT NULL, 
	deactivationDate TIMESTAMP NULL,
	PRIMARY KEY (localEntity, peerEntity, persistentId)
	);
