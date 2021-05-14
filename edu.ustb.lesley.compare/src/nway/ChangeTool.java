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
				// 修改类的名称
				if (random.nextDouble() >= 0.8) {
					c.setName(c.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("修改后类的名称为：" + c.getName());
				}
				// 新加属性，如果还要指定类型的话，之前需要缓存下EDataType
				if (random.nextDouble() >= 0.8) {
					EAttribute createEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
					createEAttribute.setName(RandomStringUtils.randomAlphanumeric(3));
					c.getEStructuralFeatures().add(createEAttribute);
					System.out.println("新加的属性名：" + createEAttribute.getName());
				}
				// 新加方法
				if (random.nextDouble() >= 0.8) {
					EOperation createEOperation = EcoreFactory.eINSTANCE.createEOperation();
					createEOperation.setName(RandomStringUtils.randomAlphanumeric(3));
					c.getEOperations().add(createEOperation);
					System.out.println("新加的方法名：" + createEOperation.getName());
				}
			}

			else if (e instanceof EAttribute) {
				// 删除某些属性
				if (random.nextDouble() >= 0.8) {
					dList.add(e);
					System.out.println("删除的属性为：" + e);
				} else if (random.nextDouble() >= 0.7) {
					// 修改某些属性
					EAttribute a = (EAttribute) e;
					a.setName(a.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("修改后的属性名称为：" + a.getName());
				}
			}

			else if (e instanceof EReference) {
				// 修改关联的名称
				if (random.nextDouble() >= 0.8) {
					EReference r = (EReference) e;
					r.setName(r.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("修改后关联的名称为：" + r.getName());
				}
			}

			else if (e instanceof EOperation) {
				// 删除某些方法
				if (random.nextDouble() >= 0.8) {
					dList.add(e);
					System.out.println("删除的方法为：" + e);
				}
			}

			// 新加类
			if (random.nextDouble() >= 0.9) {
				EClass createEClass = EcoreFactory.eINSTANCE.createEClass();
				createEClass.setName(RandomStringUtils.randomAlphanumeric(3));
				rContent.getEClassifiers().add(createEClass);
				System.out.println("新加的类：" + createEClass);
			}

			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);

		ChangeTool.save(resource, m0URI);

	}

	public static void changeForXMI(Resource resource, URI m0URI) {

		EObject rContent = resource.getContents().get(0);
		Random random = new Random();
		Collection<EObject> doNotDelete = new HashSet<>();
		Collection<EObject> dList = new HashSet<>();

		// 删除不影响其它的对象，遍历一次
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

		// 统一删除
		dList.removeAll(doNotDelete);
		dList.forEach(e -> {
			System.out.println("删除的对象：" + e);
		});
		EcoreUtil.removeAll(dList);

		// 再遍历一次
		rContent.eAllContents().forEachRemaining(e -> {

			EClass eClass = e.eClass();

			eClass.getEAllAttributes().forEach(a -> {
				// 修改属性值，给定一个概率
				// 只修改没有被关联的，不然匹配有问题
				// 只修改不作为container的，不然匹配有问题
				// 只修改不移动的，不然匹配有问题
				if (doNotDelete.contains(e) == false) {
					if (eClass.getEAllReferences().size() == 0 && random.nextDouble() >= 0.9) {
						setAttribute(e, a);
					} else if (a.isMany() && random.nextDouble() >= 0.9) {
//						// 移动多值属性
//						// A B C -> C B A
//						List<Object> targets = (List<Object>) e.eGet(a); // 注意要用Object
//						if (targets.size() >= 3) {
//							Object removeA = targets.remove(0);
//							targets.add(2, removeA);
//							Object removeB = targets.remove(1);
//							targets.add(0, removeB);
//							System.out.println("移动后的多值属性：" + targets);
//							moveSize += 2;
//						}

					}
				}

			});

			eClass.getEAllReferences().forEach(r -> {
				if (r.isContainment()) {
					// 移动Containment为true的多值引用(例如colleges)，给定一个概率
					// 移动的不新加
					if (r.isMany() && random.nextDouble() >= 0.9) {
						// 随机打乱
						List<EObject> targets = (List<EObject>) e.eGet(r);
						int N = targets.size();
						if (N >= 5) {
							shuffle(targets, N);
						}
					} else if (r.isMany() && random.nextDouble() >= 0.9) {
						// 新加Contaiment为true的当前对象的“一对多关联”的子对象，给定一个概率
						EClass eReferenceType = r.getEReferenceType(); // 关联的另一方
						EObject create = EcoreUtil.create(eReferenceType);
						eReferenceType.getEAllAttributes().forEach(a -> {
							setAttribute(create, a); // 设置单值属性
						});
						Collection<EObject> targets = (Collection<EObject>) e.eGet(r);
						targets.add(create);
						System.out.println("新加的对象：" + create);
						addSize++;
					}

				} else {
					// 移动Containment为false的多值引用(例如friends)，给定一个概率
					if (r.isMany() && random.nextDouble() >= 0.9) {
						// A B C -> C B A
						List<EObject> targets = (List<EObject>) e.eGet(r);
						if (targets.size() >= 3) {
							EObject removeA = targets.remove(0);
							targets.add(2, removeA);
							EObject removeB = targets.remove(1);
							targets.add(0, removeB);
							System.out.println("移动Containment为false的多值引用：" + targets);
							moveSize += 2;
						}
					}
				}

			});

			System.out.println("--------------------------------------------------");

		});

		System.out.println("总共删除的个数：" + dList.size());
		System.out.println("总共新加的个数：" + addSize);
		System.out.println("总共修改属性值的个数：" + changeSize);
		System.out.println("总共移动序的个数：" + moveSize);

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

	/** 修改或设置新加的属性 */
	public static void setAttribute(EObject e, EAttribute a) {

		if (a.isMany() == false) { // 如果a是单值属性
			String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
			if (instanceTypeName.contains("String")) {
				Object eGet = e.eGet(a);
				if (eGet != null) {
					e.eSet(a, eGet + "_");
				} else {
					e.eSet(a, RandomStringUtils.randomAlphanumeric(5));
				}
				System.out.println("修改/设置后的属性：" + e.eGet(a));
				changeSize++;
			}
		}
		// PENDING：多值属性

	}

	// 保存为xmi
	public static void save(Resource resource, URI out) {
		try {
			resource.setURI(out);
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
