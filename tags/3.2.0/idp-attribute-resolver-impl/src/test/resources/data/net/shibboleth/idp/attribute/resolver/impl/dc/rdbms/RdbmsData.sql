INSERT INTO people
        (userid, name, password, homephone, mail, description)
         values (
         'PETER_THE_PRINCIPAL',
         'Peter Principal',
         'changeit',
         '555-111-2222',
         'peter.principal@shibboleth.net',
         'test principal');

INSERT INTO people
        (userid, name, password, homephone, mail, description)
         values (
         'PAUL_THE_PRINCIPAL',
         'Paul Principal',
         'changeit',
         '555-111-3333',
         'paul.principal@shibboleth.net',
         'test principal');
         
INSERT INTO people
        (userid, name, password, homephone, description)
         values (
         'PHILIP_THE_PRINCIPAL',
         'Philip Principal',
         'changeit',
         '555-111-4444',
         'test principal');

INSERT INTO people
        (userid, name, password, homephone, mail, description)
         values (
         'PHILIP_THE_PRINCIPAL',
         'Philip Principal',
         'changeit',
         '555-111-4444',
         '',
         'test principal');

INSERT INTO people
        (userid, name, password, homephone, mail, description)
         values (
         'PHILIP_THE_PRINCIPAL',
         'Philip Principal',
         'changeit',
         '555-111-4444',
         '  ',
         'test principal');

INSERT INTO people
        (userid, name, password, homephone, mail, description)
         values (
         'PHILIP_THE_PRINCIPAL',
         'Philip Principal',
         'changeit',
         '555-111-4444',
         ' phil.principal@shibboleth.net ',
         'test principal');

INSERT INTO groups (userid, name)
        values (
        'PETER_THE_PRINCIPAL',
        'group1');
        
INSERT INTO groups (userid, name)
        values (
        'PETER_THE_PRINCIPAL',
        'group2');