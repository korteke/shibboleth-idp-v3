importPackage(Packages.net.shibboleth.idp.attribute);
importPackage(Packages.java.util);
importPackage(Packages.java.lang);

attr = new IdPAttribute("ScriptedOne");
set = new HashSet(2);
set.add(new StringAttributeValue("Value 1"));
set.add(new StringAttributeValue("Value 2"));
attr.setValues(set);
connectorResults.add(attr);

attr = new IdPAttribute("TwoScripted");
set = new HashSet(4);
set.add(new StringAttributeValue("1Value"));
set.add(new StringAttributeValue("2Value"));
set.add(new Integer(4));
set.add(new StringAttributeValue("3Value"));
attr.setValues(set);
connectorResults.add(attr);

connectorResults.add(new Integer(4));


child = profileContext.getSubcontext("net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext");
attr = new IdPAttribute("ThreeScripted");
set = new HashSet(1);
set.add(new StringAttributeValue(child.getClass().getSimpleName()));
attr.setValues(set);
connectorResults.add(attr);
