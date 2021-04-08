package test;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import college.AddressBook;
import college.College;
import college.CollegeFactory;
import college.Person;

public class ChangeToolForCollege {

	private Resource resource = null;

	public void change(Resource resource, int start, int changeCount, int deleteCount) {

		this.resource = resource;
		AddressBook addressbook = (AddressBook) resource.getContents().get(0);
		ArrayList<College> dList = new ArrayList();

		EList<Person> persons = addressbook.getPersons();
		for (Person p : persons) {
			if (start > 0) {
				start--;
			} else {
				if (changeCount > 0) { // 修改Person的属性名
					p.setName(p.getName() + "_change");
					changeCount--;
				} else {
					EList<College> colleges = p.getColleges();
					for (College c : colleges) {
						// 删除College
						if (deleteCount > 0) {
							dList.add(c);
							deleteCount--;
						} else {
							break;
						}
					}
				}
			}
		}

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);

	}
	
	// 新加节点
	public void add(int addCount,  String addCountName) {
		
		if(resource != null) {
			AddressBook addressbook = (AddressBook) resource.getContents().get(0);
			
			while (addCount > 0) {
				Person newPerson = CollegeFactory.eINSTANCE.createPerson();
				newPerson.setName(addCountName + addCount);
				newPerson.setAge(18);
				addressbook.getPersons().add(newPerson);
				addCount--;
			}

		}else {
			System.out.println("Please run change() before add().");
		}

	}

	// 保存为xmi
	public void save(URI out) {
		try {
			resource.setURI(out);
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
