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

	private int start = 0; // 从第1个属性开始（下标对应是0）
	private int changeCount = 0; // 改变0个属性
	private int deleteCount = 0; // 删除0个属性
	private int addCount = 0; // 新加类的个数
	private String addCountName = "NewClass"; // 方便给新加类命名
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
					if (e instanceof EClass) { // 针对每一个EClass遍历
						EClass ec = (EClassImpl) e;
						EList<EStructuralFeature> eAllStructuralFeatures = ec.getEAllStructuralFeatures();
						for (EStructuralFeature a : eAllStructuralFeatures) {
							if (a instanceof EAttribute) {
								EAttribute attr = (EAttributeImpl) a;
								System.out.println(attr.getName());
								if (start > 0) {
									start--;
								} else {
									if (changeCount > 0) { // 修改属性名
										a.setName(attr.getName() + "_change");
										changeCount--;
									} else if (deleteCount > 0) { // 删除一些属性，先保存到dList中
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

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);

		// 添加新的类
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
