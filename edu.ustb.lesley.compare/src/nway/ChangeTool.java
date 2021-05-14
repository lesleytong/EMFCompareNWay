package nway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.emf.common.util.EList;
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

	static int changeSize = 0;
	static int moveSize = 0;
	static int addSize = 0;

	public static void changeForEcore(Resource resource, URI m0URI) {

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

		ChangeTool.save(resource, m0URI);

	}

	public static void changeForXMI(Resource resource, URI m0URI) {

		EObject rContent = resource.getContents().get(0);
		Random random = new Random();
		Collection<EObject> doNotDelete = new HashSet<>();
		Collection<EObject> dList = new HashSet<>();

		// ɾ����Ӱ�������Ķ��󣬱���һ��
		rContent.eAllContents().forEachRemaining(e -> {

			EList<EReference> eAllReferences = e.eClass().getEAllReferences();

			if (eAllReferences.size() == 0 && random.nextDouble() >= 0.9) {
				dList.add(e);
			}

			eAllReferences.forEach(r -> {
				if (r.isMany()) {
					Collection<EObject> targets = (Collection<EObject>) e.eGet(r);
					if (targets.size() != 0) {
						doNotDelete.addAll(targets);
					}
				} else {
					EObject target = (EObject) e.eGet(r);
					if (target != null) {
						doNotDelete.add(e);
					}
				}
			});

		});

		doNotDelete.forEach(e -> {
			System.out.println("doNotDelete e: " + e);
		});
		System.out.println("doNotDelete.size(): " + doNotDelete.size());

		// ͳһɾ��
		dList.removeAll(doNotDelete);
		dList.forEach(e -> {
			System.out.println("ɾ���Ķ���" + e);
		});
		EcoreUtil.removeAll(dList);

		// �ٱ���һ��
		rContent.eAllContents().forEachRemaining(e -> {

			EClass eClass = e.eClass();

			eClass.getEAllAttributes().forEach(a -> {
				// �޸�����ֵ������һ������
				// ֻ�޸�û�б������ģ���Ȼƥ��������
				// ֻ�޸Ĳ���Ϊcontainer�ģ���Ȼƥ��������
				// ֻ�޸Ĳ��ƶ��ģ���Ȼƥ��������
				if (doNotDelete.contains(e) == false) {
					if (eClass.getEAllReferences().size() == 0 && random.nextDouble() >= 0.9) {
						setAttribute(e, a);
					} else if (a.isMany() && random.nextDouble() >= 0.9) {
//						// �ƶ���ֵ����
//						// A B C -> C B A
//						List<Object> targets = (List<Object>) e.eGet(a); // ע��Ҫ��Object
//						if (targets.size() >= 3) {
//							Object removeA = targets.remove(0);
//							targets.add(2, removeA);
//							Object removeB = targets.remove(1);
//							targets.add(0, removeB);
//							System.out.println("�ƶ���Ķ�ֵ���ԣ�" + targets);
//							moveSize += 2;
//						}

					}
				}

			});

			eClass.getEAllReferences().forEach(r -> {
				if (r.isContainment()) {
					// �ƶ�ContainmentΪtrue�Ķ�ֵ����(����colleges)������һ������
					// �ƶ��Ĳ��¼�
					if (r.isMany() && random.nextDouble() >= 0.9) {
						// �������
						List<EObject> targets = (List<EObject>) e.eGet(r);
						int N = targets.size();
						if (N >= 5) {
							shuffle(targets, N);
						}
					} else if (r.isMany() && random.nextDouble() >= 0.9) {
						// �¼�ContaimentΪtrue�ĵ�ǰ����ġ�һ�Զ���������Ӷ��󣬸���һ������
						EClass eReferenceType = r.getEReferenceType(); // ��������һ��
						EObject create = EcoreUtil.create(eReferenceType);
						eReferenceType.getEAllAttributes().forEach(a -> {
							setAttribute(create, a); // ���õ�ֵ����
						});
						Collection<EObject> targets = (Collection<EObject>) e.eGet(r);
						targets.add(create);
						System.out.println("�¼ӵĶ���" + create);
						addSize++;
					}

				} else {
					// �ƶ�ContainmentΪfalse�Ķ�ֵ����(����friends)������һ������
					if (r.isMany() && random.nextDouble() >= 0.9) {
						// A B C -> C B A
						List<EObject> targets = (List<EObject>) e.eGet(r);
						if (targets.size() >= 3) {
							EObject removeA = targets.remove(0);
							targets.add(2, removeA);
							EObject removeB = targets.remove(1);
							targets.add(0, removeB);
							System.out.println("�ƶ�ContainmentΪfalse�Ķ�ֵ���ã�" + targets);
							moveSize += 2;
						}
					}
				}

			});

			System.out.println("--------------------------------------------------");

		});

		System.out.println("�ܹ�ɾ���ĸ�����" + dList.size());
		System.out.println("�ܹ��¼ӵĸ�����" + addSize);
		System.out.println("�ܹ��޸�����ֵ�ĸ�����" + changeSize);
		System.out.println("�ܹ��ƶ���ĸ�����" + moveSize);

		ChangeTool.save(resource, m0URI);

	}

	public static void shuffle(List<EObject> targets, int N) {
		Random random = new Random();
		// a list of non-repeating N random integers in range (a, b), where b is
		// exclusive
		List<Integer> randomNumbers = random.ints(0, N).distinct().limit(N).boxed().collect(Collectors.toList());

		List<EObject> shuffleList = new ArrayList<>();
		randomNumbers.forEach(i -> {
			shuffleList.add(targets.get(i));
		});
		// otherwise, it will report validating duplicate exception
		targets.clear();
		targets.addAll(shuffleList);
		// count moveSize
		for (int i = 0; i < N; i++) {
			if (i != randomNumbers.get(i)) {
				moveSize++;
			}
		}
	}

	/** �޸Ļ������¼ӵ����� */
	public static void setAttribute(EObject e, EAttribute a) {

		if (a.isMany() == false) { // ���a�ǵ�ֵ����
			String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
			if (instanceTypeName.contains("String")) {
				Object eGet = e.eGet(a);
				if (eGet != null) {
					e.eSet(a, eGet + "_");
				} else {
					e.eSet(a, RandomStringUtils.randomAlphanumeric(5));
				}
				System.out.println("�޸�/���ú�����ԣ�" + e.eGet(a));
				changeSize++;
			}
		}
		// PENDING����ֵ����

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
