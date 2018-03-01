package org.qiyi.video.svg.plugin.bean

public class LocalServiceBean implements Serializable {

    private String serviceImplField;

    private String serviceCanonicalName;

    //这个类只是处理过程中用到，最终结果是不需要的，所以使用transient修饰符
    private String enclosingClassName;

    //考虑到可能在一个类中的多个地方注册，所以这里为List<MethodBean>而非单个的MethodBean
    private List<MethodBean> methodBeanList = new ArrayList<>();

    public String getServiceCanonicalName() {
        return serviceCanonicalName;
    }

    public void setServiceCanonicalName(String serviceCanonicalName) {
        this.serviceCanonicalName = serviceCanonicalName;
    }

    public List<MethodBean> getMethodBeanList() {
        return methodBeanList;
    }

    public void setMethodBeanList(List<MethodBean> methodBeanList) {
        this.methodBeanList = methodBeanList;
    }

    public String getServiceImplField() {
        return serviceImplField;
    }

    public void setServiceImplField(String serviceImplField) {
        this.serviceImplField = serviceImplField;
    }

    public String getEnclosingClassName() {
        return enclosingClassName;
    }

    public void setEnclosingClassName(String enclosingClassName) {
        this.enclosingClassName = enclosingClassName;
    }

    public void addMethodBean(MethodBean bean) {
        this.methodBeanList.add(bean);
    }
}
