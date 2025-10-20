package com.lowdragmc.lowdraglib2.utils;

import com.lowdragmc.lowdraglib2.LDLib2;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@UtilityClass
public final class ReflectionUtils {

    public static Class<?> getRawType(Type type, Class<?> fallback) {
        var rawType = getRawType(type);
        return rawType != null ? rawType : fallback;
    }

    public static Class<?> getRawType(Type type) {
        return switch (type) {
            case Class<?> aClass -> aClass;
            case GenericArrayType genericArrayType -> getRawType(genericArrayType.getGenericComponentType());
            case ParameterizedType parameterizedType -> getRawType(parameterizedType.getRawType());
            case null, default -> null;
        };
    }

    public static <A extends Annotation> void findAnnotationClasses(Class<A> annotationClass, @Nullable Predicate<Map<String, Object>> annotationPredicate, Consumer<Class<?>> consumer, Runnable onFinished) {
        try {
            forEachModClass(clazz -> {
                var annotation = clazz.getAnnotation(annotationClass);
                if (annotation != null) {
                    var data = readAnnotationData(annotation);
                    if (annotationPredicate == null || annotationPredicate.test(data)) {
                        consumer.accept(clazz);
                    }
                }
            });
        } finally {
            onFinished.run();
        }
    }

    public static <A extends Annotation> void findAnnotationStaticField(Class<A> annotationClass, @Nullable Predicate<Map<String, Object>> annotationPredicate, BiConsumer<Field, Object> consumer, Runnable onFinished) {
        try {
            forEachModClass(clazz -> {
                for (var field : clazz.getDeclaredFields()) {
                    var annotation = field.getAnnotation(annotationClass);
                    if (annotation == null) {
                        continue;
                    }
                    var data = readAnnotationData(annotation);
                    if (annotationPredicate != null && !annotationPredicate.test(data)) {
                        continue;
                    }
                    if (!Modifier.isStatic(field.getModifiers())) {
                        LDLib2.LOGGER.error("Field is not static for notation: {} in {}", field.getName(), clazz);
                        continue;
                    }
                    try {
                        if (!field.canAccess(null)) {
                            field.setAccessible(true);
                        }
                        consumer.accept(field, field.get(null));
                    } catch (Throwable throwable) {
                        LDLib2.LOGGER.error("Failed to load static field for notation: {} in {}", field.getName(), clazz, throwable);
                    }
                }
            });
        } finally {
            onFinished.run();
        }
    }

    private static void forEachModClass(Consumer<Class<?>> consumer) {
        var loader = FabricLoader.getInstance();
        Collection<ModContainer> containers = loader.getModContainer(LDLib2.MOD_ID)
                .map(List::of)
                .orElseGet(loader::getAllMods);
        var visited = new HashSet<String>();
        for (var container : containers) {
            for (var root : container.getRootPaths()) {
                visitRoot(container, root, visited, consumer);
            }
        }
    }

    private static void visitRoot(ModContainer container, Path root, Set<String> visited, Consumer<Class<?>> consumer) {
        if (Files.isDirectory(root)) {
            walkClassFiles(container, root, visited, consumer);
            return;
        }
        if (Files.isRegularFile(root) && root.getFileName().toString().endsWith(".jar")) {
            try (FileSystem fs = FileSystems.newFileSystem(root, Map.of())) {
                for (Path fsRoot : fs.getRootDirectories()) {
                    walkClassFiles(container, fsRoot, visited, consumer);
                }
            } catch (FileSystemAlreadyExistsException ignored) {
                try {
                    var fs = FileSystems.getFileSystem(root.toUri());
                    for (Path fsRoot : fs.getRootDirectories()) {
                        walkClassFiles(container, fsRoot, visited, consumer);
                    }
                } catch (FileSystemNotFoundException ex) {
                    LDLib2.LOGGER.error("Failed to access existing filesystem for mod {} at {}", container.getMetadata().getId(), root, ex);
                }
            } catch (ProviderNotFoundException ignored) {
                walkClassFiles(container, root, visited, consumer);
            } catch (IOException ex) {
                LDLib2.LOGGER.error("Failed to open mod jar {} from {}", root, container.getMetadata().getId(), ex);
            }
        }
    }

    private static void walkClassFiles(ModContainer container, Path root, Set<String> visited, Consumer<Class<?>> consumer) {
        try (var stream = Files.walk(root)) {
            stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class"))
                    .forEach(path -> handleClassFile(container, root, path, visited, consumer));
        } catch (UncheckedIOException ex) {
            LDLib2.LOGGER.error("Failed to walk classes for mod {} at {}", container.getMetadata().getId(), root, ex);
        } catch (IOException ex) {
            LDLib2.LOGGER.error("Failed to walk classes for mod {} at {}", container.getMetadata().getId(), root, ex);
        }
    }

    private static void handleClassFile(ModContainer container, Path root, Path classFile, Set<String> visited, Consumer<Class<?>> consumer) {
        var className = toClassName(root, classFile);
        if (className == null) {
            return;
        }
        var key = container.getMetadata().getId() + ':' + className;
        if (!visited.add(key) || className.endsWith("module-info") || className.endsWith("package-info")) {
            return;
        }
        try {
            var clazz = Class.forName(className, false, ReflectionUtils.class.getClassLoader());
            consumer.accept(clazz);
        } catch (Throwable throwable) {
            if (LDLib2.LOGGER.isDebugEnabled()) {
                LDLib2.LOGGER.debug("Skipping class {} from mod {} while scanning annotations", className, container.getMetadata().getId(), throwable);
            }
        }
    }

    @Nullable
    private static String toClassName(Path root, Path classFile) {
        Path relative;
        try {
            relative = root.relativize(classFile);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        var path = relative.toString();
        if (!path.endsWith(".class") || path.startsWith("META-INF")) {
            return null;
        }
        path = path.substring(0, path.length() - 6);
        path = path.replace('/', '.').replace('\\', '.');
        return path.isEmpty() ? null : path;
    }

    private static Map<String, Object> readAnnotationData(Annotation annotation) {
        var result = new HashMap<String, Object>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            if (method.getParameterCount() > 0) {
                continue;
            }
            try {
                if (!method.canAccess(annotation)) {
                    method.setAccessible(true);
                }
                var value = method.invoke(annotation);
                result.put(method.getName(), normalizeAnnotationValue(value));
            } catch (Throwable throwable) {
                LDLib2.LOGGER.error("Failed to read annotation property {} on {}", method.getName(), annotation.annotationType(), throwable);
            }
        }
        return result;
    }

    private static Object normalizeAnnotationValue(Object value) {
        if (value == null) {
            return null;
        }
        var type = value.getClass();
        if (type.isArray()) {
            var length = Array.getLength(value);
            var list = new ArrayList<>(length);
            for (var i = 0; i < length; i++) {
                list.add(normalizeAnnotationValue(Array.get(value, i)));
            }
            return list;
        }
        if (value instanceof Annotation annotation) {
            return readAnnotationData(annotation);
        }
        return value;
    }
}
