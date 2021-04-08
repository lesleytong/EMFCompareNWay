package nway;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChangeToolForEcore {

	private Resource resource;

	private int start = 0; // �ӵ�1�����Կ�ʼ���±��Ӧ��0��
	private int changeCount = 0; // �ı�0������
	private int deleteCount = 0; // ɾ��0������
	private int addCount = 0; // �¼���ĸ���
	private String addCountName = "NewClass"; // ������¼�������
	ArrayList<EStructuralFeature> dList = new ArrayList();

	public void set(int start, int changeCount, int deleteCount, int addCount, String addCountName) {
		this.start = start - 1;
		this.changeCount = changeCount;
		this.deleteCount = deleteCount;
		this.addCount = addCount;
		this.addCountName = addCountName;
	}

	public void change(Resource resource) {

		this.resource = resource;

		EList<EObject> contents = resource.getContents();

		for (int i = 0; i < contents.size(); i++) {
			EObject content = contents.get(i);
			if (content instanceof EPackage) {
				EPackage ep = (EPackageImpl) content;
				EList<EObject> epContents = ep.eContents();

				for (EObject e : epContents) {
					if (e instanceof EClass) { // ���ÿһ��EClass����
						EClass ec = (EClassImpl) e;
						EList<EStructuralFeature> eAllStructuralFeatures = ec.getEAllStructuralFeatures();
						for (EStructuralFeature a : eAllStructuralFeatures) {
							if (a instanceof EAttribute) {
								EAttribute attr = (EAttributeImpl) a;
								System.out.println(attr.getName());
								if (start > 0) {
									start--;
								} else {
									if (changeCount > 0) { // �޸�������
										a.setName(attr.getName() + "_change");
										changeCount--;
									} else if (deleteCount > 0) { // ɾ��һЩ���ԣ��ȱ��浽dList��
										dList.add(a);
										deleteCount--;
									}
								}
							}
						}

					}
					System.out.println("***************************************");
				}

			}

		}

		// ����EcoreUtil�ķ�������ɾ��
		EcoreUtil.removeAll(dList);

		// ����µ���
		EClass newClass = null;
		while (addCount > 0) {
			newClass = EcoreFactory.eINSTANCE.createEClass();
			newClass.setName(addCountName + (addCount));
			EPackage ePackage = (EPackageImpl) resource.getContents().get(0);
			ePackage.getEClassifiers().add(newClass);
			addCount--;
		}

		TreeIterator<EObject> allContents = resource.getAllContents();
		while (allContents.hasNext()) {
			System.out.println(allContents.next());
		}

	}

	// ����Ϊxmi
	public void save(URI out) {
		try {
			resource.setURI(out);
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
