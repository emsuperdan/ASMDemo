package countrydiff_gradle_plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author dan.tang* @ClassName CustomPlugin* @date 2022/7/5 4:07 下午
 */
class CustomPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);

        if (isApp){
            AppExtension android = project.getExtensions().getByType(AppExtension.class);
            CustomTransform transformImpl = new CustomTransform(project);

            ArrayList<ScanSetting> list = new ArrayList<>(1);
            list.add(new ScanSetting("MetaLoad"));
            CustomTransform.registerList = list;
            android.registerTransform(transformImpl);
        }
    }

}
