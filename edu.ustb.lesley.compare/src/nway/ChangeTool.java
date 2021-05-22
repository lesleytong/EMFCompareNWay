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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChangeTool {

	static int changeSize = 0;
	static int addSize = 0;
	static boolean moveFlag = false;

	public static void changeForEcore(Resource resource, URI m0URI) {

		changeSize = 0;
		addSize = 0;

		ArrayList<EObject> dList = new ArrayList<>();
		EPackage rContent = (EPackage) resource.getContents().get(0);
		Random random = new Random();

		rContent.eContents().forEach(e -> {

			if (e instanceof EClass) {

				EClass c = (EClass) e;

				if (random.nextBoolean() == true) {

					// �޸��������
					if (random.nextDouble() >= 0.9) {
						c.setName(c.getName() + "_");
						System.out.println("�޸ĺ��������Ϊ��" + c.getName());
						changeSize++;
					}

					// ɾ��ĳЩ����
					c.getEAttributes().forEach(a -> {
						if (random.nextDouble() >= 0.9) {
							dList.add(a);
							System.out.println("ɾ��������Ϊ��" + a);
						}
					});

					// �޸����õ�����
					c.getEReferences().forEach(r -> {
						if (random.nextDouble() >= 0.9) {
							r.setName(r.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
							System.out.println("�޸ĺ����õ�����Ϊ��" + r.getName());
							changeSize++;
						}
					});

					// ɾ��ĳЩ����
					c.getEOperations().forEach(o -> {
						if (random.nextDouble() >= 0.9) {
							dList.add(o);
							System.out.println("ɾ���Ĳ���Ϊ��" + o);
						}
					});

					// �¼����ԣ������Ҫָ�����͵Ļ���֮ǰ��Ҫ������EDataType
					if (random.nextDouble() >= 0.9) {
						EAttribute createEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
						createEAttribute.setName(RandomStringUtils.randomAlphanumeric(5));
						c.getEStructuralFeatures().add(createEAttribute);
						System.out.println("�¼ӵ����ԣ�" + createEAttribute.getName());
						addSize++;
					}

					// �¼Ӳ���
					if (random.nextDouble() >= 0.9) {
						EOperation createEOperation = EcoreFactory.eINSTANCE.createEOperation();
						createEOperation.setName(RandomStringUtils.randomAlphanumeric(5));
						c.getEOperations().add(createEOperation);
						System.out.println("�¼ӵĲ�����" + createEOperation.getName());
						addSize++;
					}

				} else { // һ������ֻ��ѡ���������ɾ�ģ�����������ƶ�

					// �ƶ������Ժ�����
					if (random.nextDouble() >= 0.9) {
						EList<EStructuralFeature> eStructuralFeatures = c.getEStructuralFeatures();
						int N = eStructuralFeatures.size();
						if (N >= 3) {
							for (int i = 0; i < N; i++) {
								int randomIndexToSwap = random.nextInt(N);
								eStructuralFeatures.move(randomIndexToSwap, i);
							}
							moveFlag = true;
						}
					}

					// �ƶ��򣺲���
					if (random.nextDouble() >= 0.9) {
						EList<EOperation> eOperations = c.getEOperations();
						int M = eOperations.size();
						if (M >= 3) {
							for (int i = 0; i < M; i++) {
								int randomIndexToSwap = random.nextInt(M);
								eOperations.move(randomIndexToSwap, i);
							}
							moveFlag = true;
						}
					}
				}
			}
		});

		// ����EcoreUtil�ķ�������ɾ��
		EcoreUtil.removeAll(dList);

		System.out.println("\n");
		System.out.println("�ܹ�ɾ���ĸ�����" + dList.size());
		System.out.println("�ܹ��¼ӵĸ�����" + addSize);
		System.out.println("�ܹ��޸ĵĸ�����" + changeSize);
		if (moveFlag == true) {
			System.out.println("��������ƶ�");
		}

		ChangeTool.save(resource, m0URI);

	}

	public static void changeForXMI(Resource resource, URI m0URI) {

		addSize = 0;
		changeSize = 0;

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

//				if (a.isMany() == false ) {
//					if (random.nextDouble() >= 0.9) {
//						setAttribute(e, a);
//					}
//				}

				if (eClass.getEAllReferences().size() == 0 && a.isMany() == false) {
					if (random.nextDouble() >= 0.9) {
						setAttribute(e, a);
					}
				}

				// �޸�����ֵ������һ������
				// ֻ�޸�û�б����õģ���Ȼƥ��������
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
						if (targets.size() >= 4) {
							shuffle(targets);
							moveFlag = true;
						}
					} else if (r.isMany() && random.nextDouble() >= 0.9) {
						// �¼�ContaimentΪtrue�ĵ�ǰ����ġ�һ�Զ����á����Ӷ��󣬸���һ������
						EClass eReferenceType = r.getEReferenceType(); // ���õ���һ��
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
						// �������
						List<EObject> targets = (List<EObject>) e.eGet(r);
						if (targets.size() >= 4) {
							shuffle(targets);
							moveFlag = true;
						}
					}
				}

			});

			System.out.println("--------------------------------------------------");

		});

		System.out.println("�ܹ�ɾ���ĸ�����" + dList.size());
		System.out.println("�ܹ��¼ӵĸ�����" + addSize);
		System.out.println("�ܹ��޸�����ֵ�ĸ�����" + changeSize);
		if (moveFlag == true) {
			System.out.println("��������ƶ�");
		}

		ChangeTool.save(resource, m0URI);

	}

	public static void shuffle(List<EObject> targets) {
		int N = targets.size();
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
