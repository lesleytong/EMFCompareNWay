package nway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChangeTool {

	
	public static void changeForEcore(Resource resource) {

		ArrayList<EObject> dList = new ArrayList<>();
		EObject rContent = resource.getContents().get(0);
		Random random = new Random();

		TreeIterator<EObject> eAllContents = rContent.eAllContents();
		eAllContents.forEachRemaining(e -> {
			// ����һ������
			double nextDouble = random.nextDouble();
			if (nextDouble >= 0.6) {
				if (e instanceof EClass) {	// �޸��������
					EClass c = (EClass) e;
					c.setName(c.getName() + "_change");
					System.out.println(c.getName());
				} else if (e instanceof EDataType) {
					// do nothing
				} else if (e instanceof EAttribute) {	// ɾ��ĳЩ����
					dList.add(e);
					System.out.println(e);
				} else if (e instanceof EReference) {	// �޸����õ�����
					EReference r = (EReference) e;
					r.setName(r.getName() + "_change");
					System.out.println(r.getName());
				} else if (e instanceof EOperation) {	// ɾ��ĳЩ����
					dList.add(e);
					System.out.println(e);
				}
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// ����EcoreUtil�ķ�������ɾ��
		EcoreUtil.removeAll(dList);

	}

	public static void changeForXMI(Resource resource) {

		EObject rContent = resource.getContents().get(0);
		Random random = new Random();
		ArrayList<EObject> dList = new ArrayList<>();

		TreeIterator<EObject> eAllContents = rContent.eAllContents();
		eAllContents.forEachRemaining(e -> {
			EClass eClass = e.eClass();			

			// ��֤��֮����ܱ�ɾ���Ķ��󣨲���Ϊcontainer���������޸ġ�
			int size = eClass.getEAllContainments().size();

			// �޸�����ֵ
			eClass.getEAllAttributes().forEach(a -> {
				// ����һ������
				if (size != 0 && random.nextDouble() >= 0.8) {
					String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
					if (instanceTypeName.contains("String")) {
						String eGet = (String) e.eGet(a);
						e.eSet(a, eGet + "_change");
					} else if (instanceTypeName.contains("int") || instanceTypeName.contains("Int")) {
						Integer eGet = (Integer) e.eGet(a);
						e.eSet(a, 99);
					}
				}

			});

			// ɾ������Ϊcontainer�Ķ���
			// ����һ������
			if (size == 0 && random.nextDouble() >= 0.9) {
				System.out.println(e);
				dList.add(e);
			}
			
//			// �¼���ô����
//			if(random.nextDouble() >= 0.7) {				
//				EObject eContainer = e.eContainer();
//				EObject create = EcoreUtil.create(eClass);
//				System.out.println(create);
//				EcoreUtil.
//			}
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// ����EcoreUtil�ķ�������ɾ��
		EcoreUtil.removeAll(dList);
	

	}
	
	
	

	// ����Ϊxmi
	public static void save(Resource resource, URI out) {
		try {
			resource.setURI(out);
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
