pluginManagement {
    repositories {
        // 阿里云镜像（覆盖 Maven Central、Google、JCenter 等）
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        maven { setUrl("https://maven.aliyun.com/repository/google/") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter/") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin/") }
        // 华为云镜像
        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        // 腾讯云镜像
        maven { setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        // 网易镜像
        maven { setUrl("https://mirrors.163.com/maven/repository/maven-public/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像（覆盖 Maven Central、Google、JCenter 等）
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        maven { setUrl("https://maven.aliyun.com/repository/google/") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter/") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin/") }
        // 华为云镜像
        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        // 腾讯云镜像
        maven { setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        // 网易镜像
        maven { setUrl("https://mirrors.163.com/maven/repository/maven-public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "My Application1"
include(":app")
