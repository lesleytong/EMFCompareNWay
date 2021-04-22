package nway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChangeTool {

	public static void changeForEcore(Resource resource) {

		ArrayList<EObject> dList = new ArrayList<>();
		EPackage rContent = (EPackage) resource.getContents().get(0);
		Random random = new Random();

		rContent.eAllContents().forEachRemaining(e -> {

			if (e instanceof EClass) {
				EClass c = (EClass) e;
				// �޸��������
				if (random.nextDouble() >= 0.8) {
					c.setName(c.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("�޸ĺ��������Ϊ��" + c.getName());
				}
				// �¼����ԣ������Ҫָ�����͵Ļ���֮ǰ��Ҫ������EDataType
				if (random.nextDouble() >= 0.8) {
					EAttribute createEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
					createEAttribute.setName(RandomStringUtils.randomAlphanumeric(3));
					c.getEStructuralFeatures().add(createEAttribute);
					System.out.println("�¼ӵ���������" + createEAttribute.getName());
				}
				// �¼ӷ���
				if (random.nextDouble() >= 0.8) {
					EOperation createEOperation = EcoreFactory.eINSTANCE.createEOperation();
					createEOperation.setName(RandomStringUtils.randomAlphanumeric(3));
					c.getEOperations().add(createEOperation);
					System.out.println("�¼ӵķ�������" + createEOperation.getName());
				}
			}

			else if (e instanceof EAttribute) {
				// ɾ��ĳЩ����
				if (random.nextDouble() >= 0.8) {
					dList.add(e);
					System.out.println("ɾ��������Ϊ��" + e);
				} else if (random.nextDouble() >= 0.7) {
					// �޸�ĳЩ����
					EAttribute a = (EAttribute) e;
					a.setName(a.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("�޸ĺ����������Ϊ��" + a.getName());
				}
			}

			else if (e instanceof EReference) {
				// �޸Ĺ���������
				if (random.nextDouble() >= 0.8) {
					EReference r = (EReference) e;
					r.setName(r.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("�޸ĺ����������Ϊ��" + r.getName());
				}
			}

			else if (e instanceof EOperation) {
				// ɾ��ĳЩ����
				if (random.nextDouble() >= 0.8) {
					dList.add(e);
					System.out.println("ɾ���ķ���Ϊ��" + e);
				}
			}

			// �¼���
			if (random.nextDouble() >= 0.9) {
				EClass createEClass = EcoreFactory.eINSTANCE.createEClass();
				createEClass.setName(RandomStringUtils.randomAlphanumeric(3));
				rContent.getEClassifiers().add(createEClass);
				System.out.println("�¼ӵ��ࣺ" + createEClass);
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
		Map<EObject, Collection<EObject>> addMap = new HashMap<>();
		


		rContent.eAllContents().forEachRemaining(e -> {

			EClass eClass = e.eClass();

			// ɾ������Ϊcontainer�Ķ���
			int size = eClass.getEAllContainments().size();
			if (size == 0 && random.nextDouble() >= 0.8) {
				System.out.println("ɾ���Ķ���" + e);
				dList.add(e);

			}

			if (!dList.contains(e)) {

				// �޸�����ֵ������һ������
				eClass.getEAllAttributes().forEach(a -> {
					if (random.nextDouble() >= 0.8) {
						setAttribute(e, a);
						System.out.println("�޸�����ֵ��" + e.eGet(a));
					}
				});

				// �¼ӵ�ǰ����һ�Զ���������Ӷ��󣬸���һ������
				eClass.getEAllContainments().forEach(r -> {
					if (r.isMany() && random.nextDouble() >= 0.9) {
						EClass eReferenceType = r.getEReferenceType(); // ��������һ��
						EObject create = EcoreUtil.create(eReferenceType);
						eReferenceType.getEAllAttributes().forEach(a -> {
							setAttribute(create, a);
						});
						Collection<EObject> targets = (Collection<EObject>) e.eGet(r);
						addMap.put(create, targets);
						System.out.println("�¼ӵĶ���" + create);
					}
				});
				
//				// �¼ӵ�ǰ����һ�Զ�����������ö��󣬸���һ������
//				eClass.eCrossReferences().forEach(r -> {
//					
//				});
				
				// ����root���ܱ�������������дroot�������¼ӡ�һ�Զ���������Ӷ��󣬸���һ������
				rContent.eClass().getEAllContainments().forEach(r -> {
					if (r.isMany() && random.nextDouble() >= 0.95) {
						EClass eReferenceType = r.getEReferenceType();
						EObject create = EcoreUtil.create(eReferenceType);
						eReferenceType.getEAllAttributes().forEach(a -> {
							setAttribute(create, a);
						});
						Collection<EObject> targets = (Collection<EObject>) rContent.eGet(r);
						addMap.put(create, targets);
						System.out.println("�¼ӵĶ���" + create);
					}
				});
				
			}

			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// ����EcoreUtil�ķ���ͳһɾ��
		EcoreUtil.removeAll(dList);

		// �����ͳһ�¼�
		addMap.forEach((key, value) -> {
			value.add(key);
		});
		
	}

	public static void setAttribute(EObject e, EAttribute a) {
		
		if(!a.isMany()) {	// ���a�ǵ�ֵ����
			String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
			if (instanceTypeName.contains("String")) {
				Object eGet = e.eGet(a);
				if (eGet != null) {
					e.eSet(a, eGet + "_" + RandomStringUtils.randomAlphanumeric(3));
				} else {
					e.eSet(a, RandomStringUtils.randomAlphanumeric(3));
				}
			} else if (instanceTypeName.contains("int") || instanceTypeName.contains("Int")) {
				Random random = new Random();
				int nextInt = 18 + random.nextInt(20);
				e.eSet(a, nextInt);
			}
		}
		
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
