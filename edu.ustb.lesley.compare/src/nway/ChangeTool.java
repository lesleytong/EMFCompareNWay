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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;

public class ChangeTool {

	static int changeSize = 0;
	static int addSize = 0;
	static boolean moveFlag = false;

	public static void changeForUML(Resource resource, URI m0URI) {

		changeSize = 0;
		addSize = 0;
		moveFlag = false;

		ArrayList<EObject> dList = new ArrayList<>();
		EObject model = resource.getContents().get(0);
		Random random = new Random();

		model.eAllContents().forEachRemaining(e -> {
			if (e instanceof ClassImpl) {
				ClassImpl c = (ClassImpl) e;

				if (c.isAbstract() == false) {

//					if (random.nextBoolean() == true) {

						// 修改某些Property
						c.getOwnedAttributes().forEach(a -> {
							if (random.nextDouble() >= 0.9) {
								a.setName(a.getName() + "_");
								System.out.println("修改后的Property的名称为：" + a.getName());
								changeSize++;
							}
						});

						// 删除某些Operation
						c.getOwnedOperations().forEach(o -> {
							if (random.nextDouble() >= 0.9) {
								dList.add(o);
								System.out.println("删除的Operation为：" + o);
							}
						});

//						// 新加Property
//						if (random.nextDouble() >= 0.9) {
//							Property createProperty = UMLFactory.eINSTANCE.createProperty();
//							createProperty.setName(RandomStringUtils.randomAlphanumeric(5));
//							c.getOwnedAttributes().add(createProperty);
//							System.out.println("新加的Property：" + createProperty.getName());
//							addSize++;
//						}
						
//						// 新加Operation
//						if (random.nextDouble() >= 0.9) {
//							Operation createOperation = UMLFactory.eINSTANCE.createOperation();
//							createOperation.setName(RandomStringUtils.randomAlphanumeric(5));
//							c.getOwnedOperations().add(createOperation);
//							System.out.println("新加的Opertation：" + createOperation.getName());
//							addSize++;
//						}
						
//					} else {
//						// 移动序(Property)
//						if (random.nextDouble() >= 0) {
//							EList<Property> ownedAttributes = c.getOwnedAttributes();
//							int N = ownedAttributes.size();
//							if (N >= 3) {
//								for (int i = 0; i < N; i++) {
//									int randomIndexToSwap = random.nextInt(N);
//									ownedAttributes.move(randomIndexToSwap, i);
//								}
//								moveFlag = true;
//							}
//
//						}

						// 移动序(Operation)
						if (random.nextDouble() >= 0) {
							EList<Operation> ownedOperations = c.getOwnedOperations();
							int N = ownedOperations.size();
							if (N >= 3) {
								for (int i = 0; i < N; i++) {
									int randomIndexToSwap = random.nextInt(N);
									ownedOperations.move(randomIndexToSwap, i);
								}
								moveFlag = true;
							}
						}
//					}
				}

			}

		});

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);

		System.out.println("\n");
		System.out.println("总共删除的个数：" + dList.size());
		System.out.println("总共新加的个数：" + addSize);
		System.out.println("总共修改的个数：" + changeSize);
		if (moveFlag == true) {
			System.out.println("存在序的移动");
		}

		save(resource, m0URI);

	}

	public static void changeForEcore(Resource resource, URI m0URI) {

		changeSize = 0;
		addSize = 0;
		moveFlag = false;

		ArrayList<EObject> dList = new ArrayList<>();
		Random random = new Random();
		// resource下多个package
		for (EObject ep : resource.getContents()) {
			ep.eContents().forEach(e -> {

				if (e instanceof EClass) {

					EClass c = (EClass) e;

					if (random.nextBoolean() == true) {

						// 修改类的名称
						if (random.nextDouble() >= 0.9) {
							c.setName(c.getName() + "_");
							System.out.println("修改后Class的名称为：" + c.getName());
							changeSize++;
						}

						// 删除某些属性
						c.getEAttributes().forEach(a -> {
							if (random.nextDouble() >= 0.9) {
								dList.add(a);
								System.out.println("删除的Attribute为：" + a);
							}
						});

						// 修改引用的名称
						c.getEReferences().forEach(r -> {
							if (random.nextDouble() >= 0.9) {
								r.setName(r.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
								System.out.println("修改后Reference的名称为：" + r.getName());
								changeSize++;
							}
						});

						// 删除某些操作
						c.getEOperations().forEach(o -> {
							if (random.nextDouble() >= 0.9) {
								dList.add(o);
								System.out.println("删除的Operation为：" + o);
							}
						});

						// 新加属性，如果还要指定类型的话，之前需要缓存下EDataType
						if (random.nextDouble() >= 0.9) {
							EAttribute createEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
							createEAttribute.setName(RandomStringUtils.randomAlphanumeric(5));
							c.getEStructuralFeatures().add(createEAttribute);
							System.out.println("新加的Attribute：" + createEAttribute.getName());
							addSize++;
						}

						// 新加操作
						if (random.nextDouble() >= 0.9) {
							EOperation createEOperation = EcoreFactory.eINSTANCE.createEOperation();
							createEOperation.setName(RandomStringUtils.randomAlphanumeric(5));
							c.getEOperations().add(createEOperation);
							System.out.println("新加的Opertation：" + createEOperation.getName());
							addSize++;
						}

					} else { // 一个类下只能选择上面的增删改，或者下面的移动序

						// 移动序：属性和引用
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

						// 移动序：操作
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
		}

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);

		System.out.println("\n");
		System.out.println("总共删除的个数：" + dList.size());
		System.out.println("总共新加的个数：" + addSize);
		System.out.println("总共修改的个数：" + changeSize);
		if (moveFlag == true) {
			System.out.println("存在序的移动");
		}
		save(resource, m0URI);

	}

	public static void changeForAll(Resource resource, URI m0URI) {

		addSize = 0;
		changeSize = 0;
		moveFlag = false;

		Random random = new Random();
		Collection<EObject> doNotDelete = new HashSet<>();
		Collection<EObject> dList = new HashSet<>();

		// 删除不影响其它的对象，遍历一次
		for (EObject root : resource.getContents()) {
			root.eAllContents().forEachRemaining(e -> {
				
//				// tmp for petrinet
//				EList<EReference> eAllContainments = e.eClass().getEAllContainments();
//				if(eAllContainments.size() == 0 && random.nextDouble() >= 0.9) {
//					dList.add(e);
//				}

				if (random.nextDouble() >= 0.9) {
					if(e.eContents().size() == 0) {
						dList.add(e);
					}										
				}

				EList<EReference> eReferences = e.eClass().getEReferences();
				eReferences.forEach(r -> {
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
		}

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
		for (EObject root : resource.getContents()) {
			root.eAllContents().forEachRemaining(e -> {

				EClass eClass = e.eClass();

				eClass.getEAttributes().forEach(a -> {

					// 修改属性值，给定一个概率
					// 只修改没有被引用的，不然匹配有问题
					// 只修改不作为container的，不然匹配有问题
					// 只修改不移动的，不然匹配有问题
//					if (dList.contains(e) == false) {
						
						if(random.nextDouble() >=0.9) {
							setAttribute(e, a);
						}
						
//						if (eClass.getEReferences().size() == 0 && random.nextDouble() >= 0.9) {
//							setAttribute(e, a);
//						}
//						else if (a.isMany() && random.nextDouble() >= 0.9) {
//							// 移动多值属性
//							// A B C -> C B A
//							List<Object> targets = (List<Object>) e.eGet(a); // 注意要用Object
//							if (targets.size() >= 3) {
//								Object removeA = targets.remove(0);
//								targets.add(2, removeA);
//								Object removeB = targets.remove(1);
//								targets.add(0, removeB);
//								System.out.println("移动后的多值属性：" + targets);
//								moveSize += 2;
//							}
//						}
//					}
				});

				eClass.getEReferences().forEach(r -> {

					if (r.isContainment()) {
						// 移动Containment为true的多值引用(例如colleges)，给定一个概率
						// 移动的不新加
						if (r.isMany() && random.nextDouble() >= 0) {
							
							List<EObject> targets = (List<EObject>) e.eGet(r);
							if (targets.size() >= 3) {
								shuffle(targets);
								moveFlag = true;
							}
							
//							// 随机打乱
//							List<EObject> targets = (List<EObject>) e.eGet(r);
//							if (targets.size() >= 3) {
//								EObject eObject = targets.get(0);
//								int size = eObject.eClass().getEAllContainments().size();
//								if(size == 0) {
//									shuffle(targets);
//									moveFlag = true;
//								}
//							}
						} else if (r.isMany() && random.nextDouble() >= 0.9) {
							// 新加Contaiment为true的当前对象的“一对多引用”的子对象，给定一个概率
							EClass eReferenceType = r.getEReferenceType(); // 引用的另一方
							try {
								EObject create = EcoreUtil.create(eReferenceType);
								eReferenceType.getEAttributes().forEach(a -> {
									setAttribute(create, a); // 设置单值属性
								});
								Collection<EObject> targets = (Collection<EObject>) e.eGet(r);
								targets.add(create);
								System.out.println("新加的对象：" + create);
								addSize++;
							} catch (Exception e2) {
								// do nothing
							}
						}

					} else {
//						// 移动Containment为false的多值引用(例如friends)，给定一个概率
//						if (r.isMany() && random.nextDouble() >= 0.9) {
//							// 随机打乱
//							List<EObject> targets = (List<EObject>) e.eGet(r);
//							if (targets.size() >= 3) {
//								shuffle(targets);
//								moveFlag = true;
//							}
//						}
					}

				});

				System.out.println("--------------------------------------------------");

			});
		}

		// for root
		if (moveFlag == false) {
			EObject root = resource.getContents().get(0);
			System.out.println(root);
			EClass eClass = root.eClass();
			eClass.getEReferences().forEach(r -> {
				if (r.isMany() == true) {
					
//					List<EObject> targets = (List<EObject>) root.eGet(r);
//					if (targets.size() >= 3) {
//						shuffle(targets);
//						moveFlag = true;
//					}
					
					// 随机打乱
					List<EObject> targets = (List<EObject>) root.eGet(r);
					EObject eObject = targets.get(0);
					int size = eObject.eClass().getEAllContainments().size();
					if (size == 0 && targets.size() >= 3) {
						shuffle(targets);
						moveFlag = true;
					}
				}
			});
		}

		System.out.println("总共删除的个数：" + dList.size());
		System.out.println("总共新加的个数：" + addSize);
		System.out.println("总共修改属性值的个数：" + changeSize);
		if (moveFlag == true) {
			System.out.println("存在序的移动");
		}

		save(resource, m0URI);

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

	/** 修改或设置新加的属性 */
	public static void setAttribute(EObject e, EAttribute a) {

		if (a.isMany() == false) { // 如果a是单值属性
			String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
			if (instanceTypeName.contains("String")) {
				Object eGet = e.eGet(a);
				if (eGet != null) {
					e.eSet(a, eGet + "_");
					System.out.println("修改后的属性：" + e.eGet(a));
					changeSize++;
				} else {
					e.eSet(a, RandomStringUtils.randomAlphanumeric(5));
				}
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
